package de.tototec.cmdoption;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

import de.tototec.cmdoption.handler.AddToCollectionHandler;
import de.tototec.cmdoption.handler.BooleanHandler;
import de.tototec.cmdoption.handler.BooleanOptionHandler;
import de.tototec.cmdoption.handler.ByteHandler;
import de.tototec.cmdoption.handler.CmdOptionHandler;
import de.tototec.cmdoption.handler.CmdOptionHandlerException;
import de.tototec.cmdoption.handler.EnumHandler;
import de.tototec.cmdoption.handler.IntegerHandler;
import de.tototec.cmdoption.handler.LongHandler;
import de.tototec.cmdoption.handler.PutIntoMapHandler;
import de.tototec.cmdoption.handler.StringFieldHandler;
import de.tototec.cmdoption.handler.StringMethodHandler;
import de.tototec.cmdoption.internal.F0;
import de.tototec.cmdoption.internal.F1;
import de.tototec.cmdoption.internal.FList;
import de.tototec.cmdoption.internal.I18n;
import de.tototec.cmdoption.internal.I18n.PreparedI18n;
import de.tototec.cmdoption.internal.I18nFactory;
import de.tototec.cmdoption.internal.Logger;
import de.tototec.cmdoption.internal.LoggerFactory;
import de.tototec.cmdoption.internal.Optional;
import de.tototec.cmdoption.internal.Procedure1;

/**
 * CmdOption main entry point to configure the parser, parse the command line
 * and provide help.
 * <p>
 * The central method to parse a command line is {@link #parse(String...)}.
 * <p>
 * The command line will be parsed and validated based on configuration objects
 * which are annotated with CmdOption-specific annotations, which are:
 * <ul>
 * <li>{@link CmdOption}
 * <li>{@link CmdCommand}
 * <li>{@link CmdOptionDelegate}
 * </ul>
 * <p>
 * Each parsed option will be directly applied to the corresponding method or
 * field. The configuration objects are typically provided as constructor
 * arguments, but it is also possible to use the {@link #addObject(Object...)}
 * method to add additional configuration objects.
 */
public class CmdlineParser {

	private final I18n i18n = I18nFactory.getI18n(CmdlineParser.class);

	private final Logger log = LoggerFactory.getLogger(CmdlineParser.class);

	/**
	 * The option handle handling the main parameter(s) of the command line.
	 */
	private Optional<OptionHandle> parameter = Optional.none();
	/**
	 * List of all recognized option handles.
	 */
	private final List<OptionHandle> options = new LinkedList<OptionHandle>();
	/**
	 * Map from option name to option handle.
	 */
	private final Map<String, OptionHandle> quickOptionMap = new LinkedHashMap<String, OptionHandle>();
	/**
	 * List of all recognized command handles.
	 */
	private final List<CommandHandle> commands = new LinkedList<CommandHandle>();
	/**
	 * Map from command name to command handle.
	 */
	private final Map<String, CommandHandle> quickCommandMap = new LinkedHashMap<String, CommandHandle>();

	/**
	 * The command name to use, if no command was given AND no parameters are
	 * defined.
	 */
	private String defaultCommandName = null;

	private final Map<Class<? extends CmdOptionHandler>, CmdOptionHandler> handlerRegistry;
	private UsageFormatter2 usageFormatter;
	private String programName;
	private String parsedCommandName;
	private String aboutLine;

	private boolean debugAllowed = true;
	private boolean debugMode = false;
	final String DEBUG_PREFIX = "CMDOPTION_DEBUG: ";

	private final CmdlineParser parent;

	private ResourceBundle resourceBundle;

	private Optional<String> argsFromFilePrefix = Optional.some("@");

	private Optional<String> aggregateShortOptionsWithPrefix = Optional.none();

	private boolean stopAcceptOptionAfterParameterIsSet = false;

	/**
	 * The constructor is only intended for internal use. It's used to parse sub-commands.
	 *
	 * @param parent        The parent parser.
	 * @param commandName   The command name.
	 * @param commandObject The target object holding the parsed options.
	 */
	protected CmdlineParser(final CmdlineParser parent, final String commandName, final Object commandObject) {
		this.parent = parent;
		debugAllowed = parent.debugAllowed;
		debugMode = parent.debugMode;
		programName = commandName;
		handlerRegistry = parent.handlerRegistry;
		resourceBundle = parent.resourceBundle;
		argsFromFilePrefix = parent.argsFromFilePrefix;
		usageFormatter = parent.usageFormatter;

		// TODO: should we set the commands description as about line?

		addOptions(commandObject);
	}

	/**
	 * Create a new commandline parser instance and scan all given object for
	 * supported options, parameters and commands using the pre-registered default
	 * handlers. Please note that if you want to use a custom set of option
	 * handlers, you should not give your config objects here but use the
	 * {@link #addObject(Object...)} method after you registered the desired set of
	 * handlers.
	 *
	 * @param objects The configuration objects containing supported annotations.
	 */
	public CmdlineParser(final Object... objects) {
		parent = null;
		programName = "<main class>";
		usageFormatter = new DefaultUsageFormatter2(true, 80, new TtyLineLengthDetector());

		// ensure order by using a LinkedHashMap
		handlerRegistry = new LinkedHashMap<Class<? extends CmdOptionHandler>, CmdOptionHandler>();

		FList.foreach(defaultHandlers(), new Procedure1<CmdOptionHandler>() {
			public void apply(final CmdOptionHandler h) {
				registerHandler(h);
			}
		});

		addObject(objects);
	}

	public List<CmdOptionHandler> defaultHandlers() {
		return Arrays.asList(
			new BooleanOptionHandler(),
			new BooleanHandler(),
			new StringFieldHandler(),
			new PutIntoMapHandler(),
			new AddToCollectionHandler(),
			new StringMethodHandler(),
			new LongHandler(),
			new IntegerHandler(),
			new ByteHandler(),
			new EnumHandler());
	}

	private void debug(final String msg, final Object... args) {
		// always log.debug
		if (log.isDebugEnabled()) {
			if (args == null || args.length == 0) {
				log.debug(msg);
			} else {
				log.debug(MessageFormat.format(msg, args));
			}
		}

		if (parent != null) {
			parent.debug(msg, args);
		} else {
			if (debugMode) {
				if (args == null || args.length == 0) {
					System.out.println(DEBUG_PREFIX + msg);
				} else {
					System.out.println(DEBUG_PREFIX + MessageFormat.format(msg, args));
				}
			}
		}
	}

	/**
	 * Programmatically enable or disable the debug mode.
	 */
	public void setDebugMode(final boolean debugMode) {
		this.debugMode = debugMode;
		FList.foreach(commands, new Procedure1<CommandHandle>() {
			@Override
			public void apply(CommandHandle c) {
				c.getCmdlineParser().setDebugMode(debugMode);
			}
		});
	}

	/**
	 * Allow or disallow the recognition of a request of the debug mode via the
	 * special command line option --CMDOPTION_DEBUG.
	 */
	public void setDebugModeAllowed(final boolean debugAllowed) {
		this.debugAllowed = debugAllowed;
	}

	public void setUsageFormatter(final UsageFormatter2 usageFormatter) {
		this.usageFormatter = usageFormatter;
	}

	// TODO: enable some kind of help-scanner. It should not validate the
	// commandline
	// public void parseHelp(String... cmdline) {
	//
	// }

	public void setStopAcceptOptionsAfterParameterIsSet(boolean stopAcceptOptionAfterParameterIsSet) {
		this.stopAcceptOptionAfterParameterIsSet = stopAcceptOptionAfterParameterIsSet;
	}

	public void setDefaultCommandName(final String defaultCommandName) {
		this.defaultCommandName = defaultCommandName;
	}

	public void setDefaultCommandClass(final Class<?> defaultCommandClass) {
		final CmdCommand anno = defaultCommandClass.getAnnotation(CmdCommand.class);
		if (anno == null) {
			throw new IllegalArgumentException(
				"Given class is not annotated with @" + CmdCommand.class.getSimpleName());
		}
		if (anno.names() == null || anno.names().length == 0 || anno.names()[0].length() == 0) {
			throw new IllegalArgumentException("Given default command class has no valid name");
		}
		setDefaultCommandName(anno.names()[0]);
	}

	public void parse(final String... cmdline) {
		parse(false, true, cmdline);
	}

	private String debugState(final String prefix) {
		return prefix + "Parameter: " + parameter.orNull() + "\n" +
			prefix + "Options: " +
			FList.mkString(options, "\n" + prefix + "  ", ",\n" + prefix + "  ", "") + "\n" +
			prefix + "Commands: " +
			FList.mkString(
				FList.map(commands, new F1<CommandHandle, String>() {
					@Override
					public String apply(CommandHandle c) {
						return c.toString() + "\n" + c.getCmdlineParser().debugState(prefix + "  | ");
					}
				}),
				"\n" + prefix + "  ", ",\n" + prefix + "  ", "") + "\n" +
			prefix + "ResourceBundle: " + resourceBundle + "\n" +
			prefix + "Locale: " + (resourceBundle == null ? null : resourceBundle.getLocale()) + "\n" +
			prefix + "CmdOptionHandlers: " +
			FList.mkString(handlerRegistry.entrySet(), "\n" + prefix + "  ", "\n" + prefix + "  ", "");
	}

	/**
	 * Parses the given commandline arguments.
	 * <p>
	 * If no error were detected and not in dryrun-mode, the result is applied to
	 * the config object(s).
	 * <p>
	 * If any errors where detected, they will be thrown as
	 * {@link CmdlineParserException}.
	 *
	 * @param dryrun                      If true, only checks for errors.
	 * @param detectHelpAndSkipValidation If true, the given cmdline is first checked for applied help
	 *                                    options. In such a case, no other validation errors will be
	 *                                    thrown.
	 * @param cmdline                     The commandline argument to be parsed.
	 * @throws CmdlineParserException If any errors were detected.
	 */
	public void parse(final boolean dryrun, final boolean detectHelpAndSkipValidation, final String... cmdline) {
		if (log.isDebugEnabled()) {
			log.debug("About to start parsing. dryrun: " + dryrun + ", detectHelpAndSkipValidation: "
				+ detectHelpAndSkipValidation + ", state: " + debugState("  "));
		}

		if (defaultCommandName != null && !quickCommandMap.containsKey(defaultCommandName)) {
			final PreparedI18n msg = i18n.preparetr("Default command \"{0}\" is not a known command.",
				defaultCommandName);
			throw new CmdlineParserException(msg.notr(), msg.tr());
		}

		final String[] cmdline0;
		if (cmdline == null) {
			cmdline0 = new String[]{};
		} else {
			// explode @-prefix args by reading them from file
			if (argsFromFilePrefix.isDefined()) {
				cmdline0 = FList.flatMap(cmdline, new F1<String, List<String>>() {
					public List<String> apply(final String arg) {
						if (arg.startsWith(argsFromFilePrefix.get())) {
							debug("Expanding {0} into argument list", arg);
							final File file = new File(arg.substring(1));
							if (file.exists() && file.isFile()) {
								try {
									final BufferedReader reader = new BufferedReader(new FileReader(file));
									final List<String> args = new LinkedList<String>();
									String line;
									while ((line = reader.readLine()) != null) {
										// if (line.trim().length() > 0) {
										args.add(line);
										// }
									}
									reader.close();
									return args;
								} catch (final FileNotFoundException e) {
									final PreparedI18n msg = i18n.preparetr("File referenced via {0} does not exist.", arg);
									throw new CmdlineParserException(msg.notr(), e, msg.tr());
								} catch (final IOException e) {
									final PreparedI18n msg = i18n.preparetr("File referenced via {0} could not be read.",
										arg);
									throw new CmdlineParserException(msg.notr(), e, msg.tr());
								}
							} else {
								final PreparedI18n msg = i18n.preparetr("File referenced via {0} does not exist.", arg);
								throw new CmdlineParserException(msg.notr(), msg.tr());
							}
						} else {
							return Arrays.asList(arg);
						}
					}
				}).toArray(new String[0]);
			} else {
				cmdline0 = cmdline;
			}
		}

		if (!dryrun) {
			debug("Parsing...");
			// Check without applying anything
			parse(true, detectHelpAndSkipValidation, cmdline0);
		}

		if (dryrun) {
			validateOptions();
		}

		// parseOptions - will be set to false, if an stopOption was found or
		// when stopAcceptOptionAfterParameterIsSet is true and an parameter was parsed.
		// when false, it means: parsing of options is no longer allowed
		boolean parseOptions = true;
		final String stopOption = "--";

		// optionCount - counts the occurrence for each option handle in the
		// cmdline for later validation
		final Map<OptionHandle, Integer> optionCount = new LinkedHashMap<OptionHandle, Integer>();
		for (final OptionHandle option : options) {
			optionCount.put(option, 0);
		}
		if (parameter.isDefined()) {
			optionCount.put(parameter.get(), 0);
		}

		// helpDetected - will be set to true, if we detect a help option while
		// parsing
		boolean helpDetected = false;

		final String aggregatePrefix = aggregateShortOptionsWithPrefix.getOrElse(new F0<String>() {
			public String apply() {
				return "";
			}
		});
		final LinkedHashMap<String, OptionHandle> shortOptionMap = new LinkedHashMap<String, OptionHandle>();
		final int aggregatePrefixSize = aggregatePrefix.length();
		if (aggregateShortOptionsWithPrefix.isDefined()) {
			final int expectedSize = 1 + aggregatePrefixSize;
			for (final Entry<String, OptionHandle> oh : quickOptionMap.entrySet()) {
				if (oh.getKey().startsWith(aggregatePrefix) && oh.getKey().length() == expectedSize) {
					shortOptionMap.put(oh.getKey().substring(aggregatePrefixSize), oh.getValue());
				}
			}
		}

		int index = -1;
		String[] rest = cmdline0;

		while (rest.length > index + 1) {
			if (index >= 0) {
				rest = Arrays.copyOfRange(rest, ++index, rest.length);
			}
			index = 0;

			// Actually iterate over the command line elements
			// for (int index = 0; index < cmdline.length; ++index) {
			final String param = rest[index];
			if (parseOptions && stopOption.equals(param)) {
				debug("Found \"" + stopOption + "\". Disabling parsing subsequent options.");
				parseOptions = false;

			} else if (debugAllowed && param.equals("--CMDOPTION_DEBUG")) {
				if (!debugMode) {
					setDebugMode(true);
					debug("Enabled debug mode\n" + debugState(""));
				}
				continue;
			} else if (parseOptions && quickOptionMap.containsKey(param)) {
				// Found an option
				final OptionHandle optionHandle = quickOptionMap.get(param);
				optionCount.put(optionHandle, optionCount.get(optionHandle) + 1);
				if (optionHandle.isHelp()) {
					debug("Detected a help request through: " + param);
					helpDetected = true;
				}

				if (rest.length <= index + optionHandle.getArgsCount()) {
					final PreparedI18n msg = i18n.preparetr(
						"Missing argument(s): {0}. Option \"{1}\" requires {2} arguments, but you gave {3}.",
						FList.mkString(
							Arrays.asList(optionHandle.getArgs()).subList(rest.length - index - 1,
								optionHandle.getArgsCount()),
							", "),
						param, optionHandle
							.getArgsCount(),
						rest.length - index - 1);
					throw new CmdlineParserException(msg.notr(), msg.tr());
				}
				// slurp next cmdline arguments into option arguments
				final String[] optionArgs = Arrays.copyOfRange(rest, index + 1,
					index + 1 + optionHandle.getArgsCount());
				index += optionHandle.getArgsCount();

				final AccessibleObject element = optionHandle.getElement();
				final CmdOptionHandler handler = optionHandle.getCmdOptionHandler();

				if (!dryrun) {
					try {
						final boolean origAccessibleFlag = element.isAccessible();
						if (!origAccessibleFlag) {
							element.setAccessible(true);
						}
						handler.applyParams(optionHandle.getObject(), element, optionArgs, param);
						if (!origAccessibleFlag) {
							// do not leave doors open
							element.setAccessible(origAccessibleFlag);
						}
					} catch (final CmdOptionHandlerException e) {
						throw new CmdlineParserException(e.getMessage(), e, e.getLocalizedMessage());
					} catch (final Exception e) {
						final PreparedI18n msg = i18n.preparetr("Could not apply parameters {0} to field/method {1}",
							Arrays.toString(optionArgs), element);
						throw new CmdlineParserException(msg.notr(), e, msg.tr());
					}
				}
				continue;
			} else if (parseOptions && quickCommandMap.containsKey(param)) {
				// Found a command
				final CommandHandle commandHandle = quickCommandMap.get(param);
				if (!dryrun) {
					parsedCommandName = param;
				}
				// Delegate parsing of the rest of the cmdline to the command
				commandHandle.getCmdlineParser().parse(dryrun, detectHelpAndSkipValidation,
					Arrays.copyOfRange(rest, index + 1, rest.length));
				// Stop parsing
				break;
			}

			// until here no single option and no command

			if (parseOptions
				&& aggregateShortOptionsWithPrefix.isDefined()
				&& param.startsWith(aggregatePrefix)
				&& param.length() > aggregatePrefixSize + 1) {

				// Found an aggregated short option

				// if true, the match is not a valid option aggregation and
				// should later be handled as normal parameter
				boolean failed = false;

				final char[] singleOptions = param.substring(aggregatePrefixSize).toCharArray();
				// rewrite the cmdline
				final List<String> rewritten = new LinkedList<String>();
				int procCount = 1;
				for (final char c : singleOptions) {
					final OptionHandle oh = shortOptionMap.get(String.valueOf(c));
					if (oh == null) {
						// unsupported aggregation found
						failed = true;
						break;
					}
					if (rest.length < procCount + oh.getArgsCount()) {
						// FIXME: missing args detected
						final PreparedI18n msg = i18n.preparetr(
							"Missing argument(s): {0}. Option \"{1}\" requires {2} arguments, but you gave {3}.",
							FList.mkString(
								Arrays.asList(oh.getArgs()).subList(rest.length - procCount,
									oh.getArgsCount()),
								", "),
							aggregatePrefix + c, oh.getArgsCount(),
							rest.length - procCount);
						throw new CmdlineParserException(msg.notr(), msg.tr());
					}
					// add as standalone short option
					rewritten.add(aggregatePrefix + c);
					for (int i = 0; i < oh.getArgsCount(); ++i) {
						// slurp args from cmdline
						rewritten.add(rest[i + procCount]);
						++procCount;
					}
				}
				if (!failed) {
					// re-iterate parsing with the modified command line
					// (backtracking)
					final String[] newRest = Arrays.copyOfRange(rest, procCount, rest.length);
					rewritten.addAll(Arrays.asList(newRest));
					rest = rewritten.toArray(new String[0]);
					index = -1;
					continue;
				}
			}

			if (parameter.isEmpty() && defaultCommandName != null
				&& quickCommandMap.containsKey(defaultCommandName)) {
				// Assume a default command inserted here
				debug("Unsupported option '" + param + "' found, assuming default command: " + defaultCommandName);
				final CommandHandle commandHandle = quickCommandMap.get(defaultCommandName);

				if (!dryrun) {
					parsedCommandName = defaultCommandName;
				}
				// Delegate parsing of the rest of the cmdline to the command
				commandHandle.getCmdlineParser().parse(dryrun, detectHelpAndSkipValidation,
					Arrays.copyOfRange(rest, index, rest.length));
				// Stop parsing
				break;

			} else if (parameter.isDefined()) {
				final OptionHandle paramHandle = parameter.get();
				// Found a parameter
				optionCount.put(paramHandle, optionCount.get(paramHandle) + 1);

				if (stopAcceptOptionAfterParameterIsSet && parseOptions) {
					debug("Found a parameter and stopAcceptOptionAfterParameterIsSet is enabled. Disabling parsing subsequent options.");
					parseOptions = false;
				}

				if (rest.length <= index + paramHandle.getArgsCount() - 1) {
					final int countOfGivenParams = rest.length - index;
					final PreparedI18n msg = i18n.preparetr(
						"Missing arguments: {0} Parameter requires {1} arguments, but you gave {2}.",
						Arrays.asList(paramHandle.getArgs()).subList(countOfGivenParams,
							paramHandle.getArgsCount()),
						paramHandle.getArgsCount(), countOfGivenParams);
					throw new CmdlineParserException(msg.notr(), msg.tr());
				}
				// slurp next cmdline arguments into option arguments
				final String[] optionArgs = Arrays.copyOfRange(rest, index, index + paramHandle.getArgsCount());
				// -1, because index gets increased by one at end of for-loop
				index += paramHandle.getArgsCount() - 1;

				final AccessibleObject element = paramHandle.getElement();
				final CmdOptionHandler handler = paramHandle.getCmdOptionHandler();

				if (!dryrun) {
					try {
						debug("Apply main parameter from parameters: {0}", FList.mkString(optionArgs, ", "));
						final boolean origAccessibleFlag = element.isAccessible();
						if (!origAccessibleFlag) {
							element.setAccessible(true);
						}
						handler.applyParams(paramHandle.getObject(), element, optionArgs, param);
						if (!origAccessibleFlag) {
							// do not leave doors open
							element.setAccessible(origAccessibleFlag);
						}
					} catch (final CmdOptionHandlerException e) {
						throw new CmdlineParserException(e.getMessage(), e, e.getLocalizedMessage());
					} catch (final Exception e) {
						final PreparedI18n msg = i18n.preparetr("Could not apply parameters {0} to field/method {1}",
							Arrays.toString(optionArgs), element);
						throw new CmdlineParserException(msg.notr(), e, msg.tr());
					}
				}

			} else {
				final PreparedI18n msg = i18n.preparetr("Unsupported option or parameter found: {0}", param);
				throw new CmdlineParserException(msg.notr(), msg.tr());
			}
		}

		if (!detectHelpAndSkipValidation || !helpDetected) {
			// Validate optionCount matches allowed
			for (final Entry<OptionHandle, Integer> optionC : optionCount.entrySet()) {
				final OptionHandle option = optionC.getKey();
				final Integer count = optionC.getValue();
				if (count < option.getMinCount() || (option.getMaxCount() > 0 && count > option.getMaxCount())) {
					final PreparedI18n rangeMsg;
					if (option.getMaxCount() < 0) {
						rangeMsg = i18n.preparetr("at least {0}", option.getMinCount());
					} else {
						if (option.getMinCount() == option.getMaxCount()) {
							rangeMsg = i18n.preparetr("exactly {0}", option.getMinCount());
						} else {
							rangeMsg = i18n.preparetr("between {0} and {1}",
								option.getMinCount(), option.getMaxCount());
						}
					}
					final String msg;
					final Object[] msgArgs;
					final Object[] msgArgsTr;
					if (option.getNames() == null || option.getNames().length == 0) {
						msg = I18n.marktr("Main parameter \"{0}\" was given {1} times, but must be given {2} times");
						msgArgs = new Object[]{FList.mkString(option.getArgs(), " "), count, rangeMsg.notr()};
						msgArgsTr = new Object[]{FList.mkString(option.getArgs(), " "), count, rangeMsg.tr()};
					} else {
						msg = I18n.marktr("Option \"{0}\" was given {1} times, but must be given {2} times");
						msgArgs = new Object[]{option.getNames()[0], count, rangeMsg.notr()};
						msgArgsTr = new Object[]{option.getNames()[0], count, rangeMsg.tr()};
					}
					throw new CmdlineParserException(MessageFormat.format(msg, msgArgs), i18n.tr(msg, msgArgsTr));
				}
			}

			// Validate required options because of 'required' attribute in
			// other options
			for (final Entry<OptionHandle, Integer> optionC : optionCount.entrySet()) {
				if (optionC.getValue() > 0) {
					final OptionHandle calledOption = optionC.getKey();
					for (final String required : calledOption.getRequires()) {
						// check, if an option was called with that name, if
						// not, this is an error
						final OptionHandle reqOptionHandle = quickOptionMap.get(required);
						if (reqOptionHandle == null) {
							// required option does not exists, error
							// TODO: error

						} else {
							final Integer reqOptionCount = optionCount.get(reqOptionHandle);
							if (reqOptionCount == null || reqOptionCount.intValue() <= 0) {
								// required option was not called, this is an
								// error
								final PreparedI18n msg = i18n.preparetr(
									"When using option \"{0}\" also option \"{1}\" must be given.",
									calledOption.getNames()[0], required);
								throw new CmdlineParserException(msg.notr(), msg.tr());
							}
						}
					}
					for (final String conflict : calledOption.getConflictsWith()) {
						// check, if an option was called with that name, if
						// not, this is an error
						final OptionHandle conflictOptionHandle = quickOptionMap.get(conflict);
						if (conflictOptionHandle == null) {
							// conflicting option does not exists, error
							// TODO: error

						} else {
							final Integer conflictOptionCount = optionCount.get(conflictOptionHandle);
							if (conflictOptionCount != null && conflictOptionCount.intValue() > 0) {
								// conflicting option was called, this is an
								// conflict
								final PreparedI18n msg = i18n.preparetr(
									"Options \"{0}\" and \"{1}\" cannot be used at the same time.",
									calledOption.getNames()[0], conflict);
								throw new CmdlineParserException(msg.notr(), msg.tr());
							}
						}
					}
				}
			}
		}

	}

	public String getParsedCommandName() {
		return parsedCommandName;
	}

	public Object getParsedCommandObject() {
		if (parsedCommandName != null) {
			// NPE not possible, because we set parsedCommandName only if the
			// command exists in the map
			return quickCommandMap.get(parsedCommandName).getObject();
		} else {
			return null;
		}
	}

	/**
	 * Find a {@link CmdOptionHandler} for the given element, argument count and
	 * requested handler type.
	 *
	 * @return The found {@link CmdOptionHandler} or <code>null</code>.
	 * @throws CmdlineParserException in case an error occured.
	 */
	protected CmdOptionHandler findHandler(final AccessibleObject element, final int argsCount,
										   final Class<? extends CmdOptionHandler> cmdOptionHandlerType) {
		CmdOptionHandler handler = null;
		if (cmdOptionHandlerType != null && !cmdOptionHandlerType.equals(CmdOptionHandler.class)) {
			// requested a specific handler
			final CmdOptionHandler dedicatedHandler;
			if (handlerRegistry.containsKey(cmdOptionHandlerType)) {
				dedicatedHandler = handlerRegistry.get(cmdOptionHandlerType);
			} else {
				try {
					dedicatedHandler = cmdOptionHandlerType.newInstance();
				} catch (final Exception e) {
					final PreparedI18n msg = i18n.preparetr("Could not create handler: {0}", cmdOptionHandlerType);
					throw new CmdlineParserException(msg.notr(), e, msg.tr());
				}
				// not registering this handler because self-introduced handler
				// (only in a specific annotation) should not be made available
				// to all other options.
			}
			if (dedicatedHandler.canHandle(element, argsCount)) {
				handler = dedicatedHandler;
			}
		} else {
			// walk through registered hander and find one
			for (final CmdOptionHandler regHandle : handlerRegistry.values()) {
				if (regHandle.canHandle(element, argsCount)) {
					handler = regHandle;
					break;
				}
			}
		}
		if (handler == null && parent != null) {
			return parent.findHandler(element, argsCount, cmdOptionHandlerType);
		} else {
			return handler;
		}
	}

	/**
	 * Add an additional configuration object containing CmdOption-specific
	 * annotations to the configuration.
	 * <p>
	 * Classed annotated with {@link CmdCommand} are registered as commands, and all
	 * found options and parameters are registered to the command.
	 *
	 * @param objects
	 * @throws CmdlineParserException if the given objects contain configutation errors or are
	 *                                inconsistent.
	 */
	public void addObject(final Object... objects) {
		for (final Object object : objects) {
			boolean commandAdded = addCommand(object);
			if (!commandAdded) {
				addOptions(object);
			}
		}
	}

	protected boolean addCommand(final Object object) {
		final CmdCommand commandAnno = object.getClass().getAnnotation(CmdCommand.class);
		if (commandAnno == null) {
			return false;
		}

		final String[] names = commandAnno.names();

		if (names == null || names.length == 0) {
			final PreparedI18n msg = i18n.preparetr("Command found without required name in: {0}", object);
			throw new CmdlineParserException(msg.notr(), msg.tr());
		}

		final CmdlineParser subCmdlineParser = new CmdlineParser(this, names[0], object);
		// TODO: set programm name
		final CommandHandle command = new CommandHandle(names, commandAnno.description(), subCmdlineParser, object,
			commandAnno.hidden());

		for (final String name : command.getNames()) {
			if (quickCommandMap.containsKey(name) || quickOptionMap.containsKey(name)) {
				final PreparedI18n msg = i18n.preparetr("Duplicate command/option name \"{0}\" found in: {1}",
					name,
					object);
				throw new CmdlineParserException(msg.notr(), msg.tr());
			}
			quickCommandMap.put(name, command);
		}
		commands.add(command);
		return true;
	}

	/**
	 * Check validity of the given configutaion classes. You should call this method
	 * from a unit test to detect errors and inconsistencies in your configuration.
	 *
	 * @throws CmdlineParserException if the configutation is not valid.
	 * @since 0.6.0
	 */
	public void validate() {
		validateOptions();
		for (final CommandHandle command : commands) {
			command.getCmdlineParser().validate();
		}
	}

	/**
	 * Do a consistency check for the given cmdoption model (all annotated opitons).
	 *
	 * @throws CmdlineParserException if the configutation is not valid.
	 */
	protected void validateOptions() {
		int noNameCount = 0;

		for (final OptionHandle optionHandle : options) {

			final String optionName;
			if (optionHandle.getNames() == null) {
				optionName = "<no name>";
				++noNameCount;
			} else {
				optionName = optionHandle.getNames()[0];
			}

			if (optionHandle.getMaxCount() >= 0 && optionHandle.getMaxCount() < optionHandle.getMinCount()) {
				final PreparedI18n msg = i18n.preparetr(
					"The option \"{0}\" has inconsistent min..max count configuration (min={1}, max={2}).",
					optionName, optionHandle.getMinCount(), optionHandle.getMaxCount());
				throw new CmdlineParserException(msg.notr(), msg.tr());
			}

			for (final String reqOptionName : optionHandle.getRequires()) {
				if (quickOptionMap.get(reqOptionName) == null) {
					// required option does not exists
					final PreparedI18n msg = i18n.preparetr(
						"The option \"{0}\" requires the unknown/missing option \"{1}\".", optionName,
						reqOptionName);
					throw new CmdlineParserException(msg.notr(), msg.tr());
				}
			}
			for (final String conflictOptionName : optionHandle.getConflictsWith()) {
				if (Arrays.asList(optionHandle.getNames()).contains(conflictOptionName)) {
					final PreparedI18n msg = i18n.preparetr("Option \"{0}\" is configured to conflicts with itself.",
						optionName);
					throw new CmdlineParserException(msg.notr(), msg.tr());
				}
				if (quickOptionMap.get(conflictOptionName) == null) {
					// required option does not exists
					final PreparedI18n msg = i18n.preparetr(
						"The option \"{0}\" conflicts with a unknown/missing option \"{1}\".",
						optionName, conflictOptionName);
					throw new CmdlineParserException(msg.notr(), msg.tr());

				}
			}
		}

		if (noNameCount > 1) {
			final PreparedI18n msg = i18n.preparetr("More than one main parameter detected ({0}).", noNameCount);
			throw new CmdlineParserException(msg.notr(), msg.tr());
		}

		// TODO: Ensure, there are no long options, starting with the aggregated
		// short
		// option prefix, and when, disable this feature
	}

	protected boolean isVisible(final Class<?> baseClass, final Member element) {
		if (baseClass == null || element == null)
			return false;

		final int modifiers = element.getModifiers();

		if (Modifier.isPublic(modifiers))
			return true;

		if (Modifier.isProtected(modifiers))
			return true;

		if (!Modifier.isPrivate(modifiers) && baseClass.getPackage().equals(element.getDeclaringClass().getPackage()))
			return true;

		return false;
	}

	protected boolean isPublicOrProtected(final Method method) {
		final int modifiers = method.getModifiers();
		return Modifier.isPublic(modifiers) || Modifier.isProtected(modifiers);
	}

	protected boolean isPackagePrivate(final Method method) {
		final int modifiers = method.getModifiers();
		return !Modifier.isPublic(modifiers) && !Modifier.isProtected(modifiers) && !Modifier.isPrivate(modifiers);
	}

	protected boolean isPrivate(final Method method) {
		final int modifiers = method.getModifiers();
		return Modifier.isPrivate(modifiers);
	}

	protected boolean containsMethod(final Iterable<Method> methods, final Method method) {
		return findMethod(methods, method) != null;
	}

	protected Method findMethod(final Iterable<Method> methods, final Method method) {
		for (final Method existsingMethod : methods) {
			if ((existsingMethod.getName()).equals(method.getName())) {
				final Class<?>[] existingTypes = existsingMethod.getParameterTypes();
				final Class<?>[] newTypes = method.getParameterTypes();
				if (existingTypes.length == newTypes.length) {
					boolean same = true;
					for (int i = 0; i < existingTypes.length; ++i) {
						same &= existingTypes[i].equals(newTypes[i]);
					}
					if (same)
						return existsingMethod;
				}
			}
		}
		return null;
	}

	protected void addOptions(final Object object) {
		final Class<?> class1 = object.getClass();

		final List<Field> fields = new LinkedList<Field>();
		final List<Method> privateMethods = new LinkedList<Method>();

		final List<Method> otherPackageNonPrivateMethods = new LinkedList<Method>();

		final List<Method> currentPackageNonPrivateMethods = new LinkedList<Method>();

		Class<?> parentClass = class1;
		while (parentClass != null && !parentClass.equals(Object.class)) {
			// We cannot override fields in child classes, so we simple collect
			// all fields we found
			fields.addAll(Arrays.asList(parentClass.getDeclaredFields()));

			// for methods, we need to respect overridden methods when
			// inspecting the parent classes
			for (final Method method : parentClass.getDeclaredMethods()) {
				if (isPrivate(method)) {
					privateMethods.add(method);
				} else if (isPublicOrProtected(method)) {
					if (!containsMethod(otherPackageNonPrivateMethods, method)
						&& !containsMethod(currentPackageNonPrivateMethods, method)) {
						currentPackageNonPrivateMethods.add(method);
					}
				} else if (isPackagePrivate(method)) {
					// if (!containsMethod(publicOrProtectedMethods, method)) {
					// method not overloaded
					if (isPackagePrivate(method)) {
						if (!containsMethod(currentPackageNonPrivateMethods, method)) {
							currentPackageNonPrivateMethods.add(method);
						}
					}
				}
			}

			final Package pack = parentClass.getPackage();
			parentClass = parentClass.getSuperclass();
			if ((pack == null && parentClass.getPackage() != null) ||
				(pack != null && !pack.equals(parentClass.getPackage()))) {
				otherPackageNonPrivateMethods.addAll(currentPackageNonPrivateMethods);
				currentPackageNonPrivateMethods.clear();
			}
		}

		// inspect elements
		final Set<AccessibleObject> elements = new LinkedHashSet<AccessibleObject>();
		elements.addAll(fields);
		elements.addAll(privateMethods);
		elements.addAll(otherPackageNonPrivateMethods);
		elements.addAll(currentPackageNonPrivateMethods);

		options.addAll(inspectElements(object, elements));
	}

	protected List<OptionHandle> inspectElements(final Object object, final Set<AccessibleObject> elements) {
		final List<OptionHandle> options = new LinkedList<OptionHandle>();

		for (final AccessibleObject element : elements) {

			final CmdOptionDelegate delegateAnno = element.getAnnotation(CmdOptionDelegate.class);

			if (element instanceof Field && delegateAnno != null) {
				debug("Found delegate object at: {0} with mode: ", element);
				try {
					final boolean origAccessibleFlag = element.isAccessible();
					final Object delegate;
					try {
						if (!origAccessibleFlag) {
							element.setAccessible(true);
						}
						delegate = ((Field) element).get(object);
					} finally {
						if (!origAccessibleFlag) {
							// do not leave doors open
							element.setAccessible(origAccessibleFlag);
						}
					}
					if (delegate != null) {
						switch (delegateAnno.value()) {
							case OPTIONS:
								addOptions(delegate);
								break;
							case COMMAND:
								addCommand(delegate);
								break;
							case COMMAND_OR_OPTIONS:
								boolean commandAdded = addCommand(delegate);
								if (!commandAdded) {
									addOptions(delegate);
								}
								break;
						}
					}
				} catch (final IllegalArgumentException e) {
					debug("Could not scan delegate object at: {0}", element);
				} catch (final IllegalAccessException e) {
					debug("Could not scan delegate object at: {0}", element);
				}
				continue;
			}

			final CmdOption anno = element.getAnnotation(CmdOption.class);
			if (anno == null) {
				continue;
			}

			if (element instanceof Field && Modifier.isFinal(((Field) element).getModifiers())) {
				debug("Detected option on final field: {0}", element);
				// continue;
			}

			final String[] names = anno.names();

			final CmdOptionHandler handler = findHandler(element, anno.args().length, anno.handler());
			if (handler == null) {
				final PreparedI18n msg = i18n.preparetr(
					"No suitable handler found for option(s): {0} ({1} argument(s))",
					FList.mkString(anno.names(), ","),
					anno.args().length);
				throw new CmdlineParserException(msg.notr(), msg.tr());
			}

			if (names == null || names.length == 0) {
				// No names means this is the ONLY parameter
				if (parameter.isDefined()) {
					final PreparedI18n msg = i18n.preparetr(
						"More than one parameter definition found. First definition: {0} Second definition: {1}",
						parameter.get().getElement(), element);
					throw new CmdlineParserException(msg.notr(), msg.tr());
				}

				if (anno.isHelp()) {
					debug("Warning: Found annotation for the main parameter with enabled isHelp=true. The isHelp will be ignored.");
				}
				if (anno.maxCount() == 0) {
					debug("Warning: Found annotation for the main parameter with maxCount=0. This is interpreted as unconstrained.");
				}

				final OptionHandle paramHandle = new OptionHandle(
					new String[]{}, anno.description(), handler,
					object, element, anno.args(), anno.minCount(), anno.maxCount(),
					false /* cannot be a help option */,
					anno.hidden(), anno.requires(), anno.conflictsWith());

				if (paramHandle.getArgsCount() <= 0) {
					final PreparedI18n msg = i18n.preparetr("Parameter definition must support at least on argument.");
					throw new CmdlineParserException(msg.notr(), msg.tr());
				}
				parameter = Optional.some(paramHandle);

			} else {
				if (anno.maxCount() == 0) {
					debug("Warning: Found annotation for option [{0}] with maxCount=0. This is interpreted as unconstrained.",
						FList.mkString(names, ","));
				}

				final OptionHandle option = new OptionHandle(names, anno.description(), handler, object,
					element, anno.args(), anno.minCount(), anno.maxCount(), anno.isHelp(), anno.hidden(),
					anno.requires(), anno.conflictsWith());

				for (final String name : names) {
					if (quickCommandMap.containsKey(name) || quickOptionMap.containsKey(name)) {
						final PreparedI18n msg = i18n.preparetr("Duplicate command/option name \"{0}\" found in: {1}",
							name, element);
						throw new CmdlineParserException(msg.notr(), msg.tr());
					}
					quickOptionMap.put(name, option);
				}
				options.add(option);
			}
		}
		return options;
	}

	public void unregisterAllHandler() {
		handlerRegistry.clear();
	}

	public void unregisterHandler(final Class<? extends CmdOptionHandler> type) {
		if (type != null) {
			handlerRegistry.remove(type);
		}
	}

	/**
	 * Register a new CmdOptionHandler. Please note: The newly registered handlers
	 * will only have an effect to succeeding calls to
	 * {@link #addObject(Object...)}.
	 */
	public void registerHandler(final CmdOptionHandler handler) {
		if (handler != null) {
			debug("Register CmdOptionHandler: {0}", handler);
			handlerRegistry.put(handler.getClass(), handler);
		}
	}

	public void commandUsage(final Class<?> command) {
		for (final CommandHandle cmdHandle : commands) {
			if (cmdHandle.getObject().getClass().equals(command)) {
				cmdHandle.getCmdlineParser().usage();
				return;
			}
		}

		throw new IllegalArgumentException("Given command is not known or does not have a @"
			+ CmdCommand.class.getSimpleName() + "-annotation");
	}

	public void usage() {
		usage(System.out);
	}

	/**
	 * @param output
	 * @deprecated Use {@link #usage(PrintStream)} instead.
	 */
	@Deprecated
	public void usage(final StringBuilder output) {
		output.append(usageString());
	}

	public void usage(final PrintStream output) {
		usageFormatter.format(output, getCmdlineModel());
	}

	public String usageString() {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final PrintStream ps = new PrintStream(baos);
		usage(ps);
		ps.flush();
		return new String(baos.toByteArray(), Charset.forName("UTF-8"));
	}

	public CmdlineModel getCmdlineModel() {
		String programName = this.programName;
		if (parent != null) {
			// We are a command
			programName = parent.programName + " " + programName;
		}
		return new CmdlineModel(programName, options, commands, parameter.orNull(), aboutLine, resourceBundle);
	}

	/**
	 * Set the name of the program is usually called on the command line.
	 */
	public void setProgramName(final String programName) {
		this.programName = programName;
	}

	/**
	 * An additional text displayed at the top of the usage/help display.
	 */
	public void setAboutLine(final String aboutLine) {
		this.aboutLine = aboutLine;
	}

	public void setResourceBundle(final String resourceBundleName, final ClassLoader classloader) {
		setResourceBundle(resourceBundleName, Locale.getDefault(), classloader);
	}

	public void setResourceBundle(final String resourceBundleName, final Locale locale, final ClassLoader classloader) {
		try {
			this.resourceBundle = ResourceBundle.getBundle(resourceBundleName, locale, classloader);
			debug("Loaded a ResourceBundle with name \"{0}\" using classloader \"{1}\". Locale: {2}",
				resourceBundleName, classloader, resourceBundle.getLocale());
		} catch (final MissingResourceException e) {
			debug("Could not load a ResourceBundle with name \"{0}\" using classloader \"{1}\" for locale {2}",
				resourceBundleName, classloader, Locale.getDefault());
			// no resource bundle found
			this.resourceBundle = null;
		}
	}

	public void setResourceBundle(final ResourceBundle resourceBundle) {
		this.resourceBundle = resourceBundle;
	}

	/**
	 * Set the argument prefix used to mark a cmdline argument as file which
	 * contains more commandline parameters. If not changed, this is by default the
	 * <code>"@"</code> sign. You can also disable this feature by setting
	 * <code>null</code> or the empty string.
	 * <p>
	 * The file contains additional arguments, each one on a new line.
	 *
	 * @param prefix The prefix to mark an argument as arguments-file or
	 *               <code>null</code> to disable the feature.
	 */
	public void setReadArgsFromFilePrefix(final String prefix) {
		if (prefix == null || prefix.trim().isEmpty()) {
			argsFromFilePrefix = Optional.none();
		} else {
			argsFromFilePrefix = Optional.some(prefix.trim());
		}
	}

	public void setAggregateShortOptionsWithPrefix(final String prefix) {
		if (prefix == null || prefix.trim().isEmpty()) {
			aggregateShortOptionsWithPrefix = Optional.none();
		} else {
			aggregateShortOptionsWithPrefix = Optional.some(prefix.trim());
		}
	}

}
