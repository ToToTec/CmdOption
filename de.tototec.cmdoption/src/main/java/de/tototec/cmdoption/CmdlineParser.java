package de.tototec.cmdoption;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
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
import de.tototec.cmdoption.handler.CmdOptionHandler;
import de.tototec.cmdoption.handler.CmdOptionHandlerException;
import de.tototec.cmdoption.handler.IntegerHandler;
import de.tototec.cmdoption.handler.PutIntoMapHandler;
import de.tototec.cmdoption.handler.StringFieldHandler;
import de.tototec.cmdoption.handler.StringMethodHandler;

/**
 * CmdOption main entry point to configure the parser, parse the command line
 * and provide help.
 * 
 * The central method to parse a command line is {@link #parse(String...)}.
 * 
 * The command line will be parsed and validated based on configuration objects
 * which are annotated with CmdOption-specific annotations, which are:
 * <ul>
 * <li>{@link CmdOption}
 * <li>{@link CmdCommand}
 * <li>{@link CmdOptionDelegate}
 * </ul>
 * 
 * Each parsed option will be directly applied to the corresponding method or
 * field. The configuration objects are typically provided as constructor
 * arguments, but it is also possible to use the {@link #addObject(Object...)}
 * method to add additional configuration objects.
 * 
 */
public class CmdlineParser {

	private final I18n i18n = I18nFactory.getI18n(CmdlineParser.class);

	private final Logger log = LoggerFactory.getLogger(CmdlineParser.class);

	/**
	 * The option handle handling the main parameter(s) of the command line.
	 */
	private OptionHandle parameter = null;
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
	private UsageFormatter usageFormatter = new DefaultUsageFormatter(true);
	private String programName;
	private String parsedCommandName;
	private String aboutLine;

	private boolean debugAllowed = true;
	private boolean debugMode = false;
	final String DEBUG_PREFIX = "CMDOPTION_DEBUG: ";

	private final CmdlineParser parent;

	private ResourceBundle resourceBundle;

	protected CmdlineParser(final CmdlineParser parent, final String commandName, final Object commandObject) {
		this.parent = parent;
		debugMode = parent.debugMode;
		programName = commandName;
		handlerRegistry = parent.handlerRegistry;
		scanOptions(commandObject);
	}

	public CmdlineParser(final Object... objects) {
		parent = null;
		programName = "<main class>";
		handlerRegistry = new LinkedHashMap<Class<? extends CmdOptionHandler>, CmdOptionHandler>();

		addObject(objects);

		registerHandler(new BooleanOptionHandler());
		registerHandler(new BooleanHandler());
		registerHandler(new StringFieldHandler());
		registerHandler(new PutIntoMapHandler());
		registerHandler(new AddToCollectionHandler());
		registerHandler(new StringMethodHandler());
		registerHandler(new IntegerHandler());
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
	}

	/**
	 * Allow or disallow the recognition of a request of the debug mode via the
	 * special command line option --CMDOPTION_DEBUG.
	 */
	public void setDebugModeAllowed(final boolean debugAllowed) {
		this.debugAllowed = debugAllowed;
	}

	public void setUsageFormatter(final UsageFormatter usageFormatter) {
		this.usageFormatter = usageFormatter;
	}

	// TODO: enable some kind of help-scanner. It should not validate the
	// commandline
	// public void parseHelp(String... cmdline) {
	//
	// }

	public void setDefaultCommandName(final String defaultCommandName) {
		this.defaultCommandName = defaultCommandName;
	}

	public void setDefaultCommandClass(final Class<?> defaultCommandClass) {
		final CmdCommand anno = defaultCommandClass.getAnnotation(CmdCommand.class);
		if (anno == null) {
			throw new IllegalArgumentException("Given class is not annotated with @" + CmdCommand.class.getSimpleName());
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
		return prefix + "Parameter: " + parameter
				+ prefix + "Options: " + Util.mkString(options, "\n  ", ",\n  ", null)
				+ prefix + "Commands: " + Util.mkString(commands, "\n  ", ",\n  ", null)
				+ prefix + "ResourceBundle: " + resourceBundle
				+ prefix + "Locale: " + (resourceBundle == null ? null : resourceBundle.getLocale())
				+ prefix + "CmdOptionHandlers: "
				+ Util.mkString(handlerRegistry.entrySet(), "\n  ", "\n" + prefix + "  ", null);
	}

	public void parse(final boolean dryrun, final boolean detectHelpAndSkipValidation, String... cmdline) {
		if (log.isDebugEnabled()) {
			log.debug("About to start parsing. dryrun: " + dryrun + ", detectHelpAndSkipValidation: "
					+ detectHelpAndSkipValidation + ", state: " + debugState("  "));
		}

		if (defaultCommandName != null && !quickCommandMap.containsKey(defaultCommandName)) {
			final String msg = I18n.marktr("Default command \"{0}\" is not a known command.");
			throw new CmdlineParserException(null, msg, defaultCommandName);
		}

		if (!dryrun) {
			debug("Parsing...");
			// Check without applying anything
			parse(true, detectHelpAndSkipValidation, cmdline);
		}

		if (dryrun) {
			validateOptions();
		}

		// Avoid null access
		cmdline = cmdline == null ? new String[] {} : cmdline;

		// Should be set to false, if an stopOption was found and parsing of
		// options is no longer allowed
		boolean parseOptions = true;
		final String stopOption = "--";

		// optionCount counts the occurrence for each option handle in the
		// cmdline
		final Map<OptionHandle, Integer> optionCount = new LinkedHashMap<OptionHandle, Integer>();
		for (final OptionHandle option : options) {
			optionCount.put(option, 0);
		}
		if (parameter != null) {
			optionCount.put(parameter, 0);
		}

		boolean helpDetected = false;

		// Actually iterate over the command line elements
		for (int index = 0; index < cmdline.length; ++index) {
			final String param = cmdline[index];
			if (parseOptions && stopOption.equals(param)) {
				parseOptions = false;

			} else if (debugAllowed && param.equals("--CMDOPTION_DEBUG")) {
				if (!debugMode) {
					debugMode = true;
					debug("Enabled debug mode\n" + debugState(""));
				}

			} else if (parseOptions && quickOptionMap.containsKey(param)) {
				// Found an option
				final OptionHandle optionHandle = quickOptionMap.get(param);
				optionCount.put(optionHandle, optionCount.get(optionHandle) + 1);
				if (optionHandle.isHelp()) {
					debug("Detected a help request though: " + param);
					helpDetected = true;
				}

				if (cmdline.length <= index + optionHandle.getArgsCount()) {
					throw new CmdlineParserException(
							null,
							I18n.marktr("Missing arguments(s): {0}. Option \"{1}\" requires {2} arguments, but you gave {3}."),
							Util.mkString(
									Arrays.asList(optionHandle.getArgs()).subList(cmdline.length - index - 1,
											optionHandle.getArgsCount()), null, ", ", null), param, optionHandle
											.getArgsCount(), cmdline.length - index - 1);
				}
				// slurp next cmdline arguments into option arguments
				final String[] optionArgs = Arrays.copyOfRange(cmdline, index + 1,
						index + 1 + optionHandle.getArgsCount());
				index += optionHandle.getArgsCount();

				final AccessibleObject element = optionHandle.getElement();
				final CmdOptionHandler handler = findHandler(element, optionHandle.getArgsCount(),
						optionHandle.getCmdOptionHandlerType());

				if (handler == null) {
					throw new CmdlineParserException(null,
							I18n.marktr("No suitable handler found for option: {0} ({1} argument(s))"), param,
							optionHandle.getArgsCount());
				}

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
						throw new CmdlineParserException(e,
								I18n.marktr("Could not apply parameters {0} to field/method {1}"),
								Arrays.toString(optionArgs), element);
					}
				}
			} else if (parseOptions && quickCommandMap.containsKey(param)) {
				// Found a command
				final CommandHandle commandHandle = quickCommandMap.get(param);
				if (!dryrun) {
					parsedCommandName = param;
				}
				// Delegate parsing of the rest of the cmdline to the command
				commandHandle.getCmdlineParser().parse(dryrun, detectHelpAndSkipValidation,
						Arrays.copyOfRange(cmdline, index + 1, cmdline.length));
				// Stop parsing
				break;

			} else if (parameter == null && defaultCommandName != null
					&& quickCommandMap.containsKey(defaultCommandName)) {
				// Assume a default command inserted here
				debug("Unsupported option '" + param + "' found, assuming default command: " + defaultCommandName);
				final CommandHandle commandHandle = quickCommandMap.get(defaultCommandName);

				if (!dryrun) {
					parsedCommandName = defaultCommandName;
				}
				// Delegate parsing of the rest of the cmdline to the command
				commandHandle.getCmdlineParser().parse(dryrun, detectHelpAndSkipValidation,
						Arrays.copyOfRange(cmdline, index, cmdline.length));
				// Stop parsing
				break;

			} else if (parameter != null) {
				// Found a parameter
				optionCount.put(parameter, optionCount.get(parameter) + 1);

				if (cmdline.length <= index + parameter.getArgsCount() - 1) {
					final int countOfGivenParams = cmdline.length - index;
					throw new CmdlineParserException(null,
							I18n.marktr("Missing arguments: {0} Parameter requires {1} arguments, but you gave {2}."),
							Arrays.asList(parameter.getArgs()).subList(countOfGivenParams, parameter.getArgsCount()),
							parameter.getArgsCount(), countOfGivenParams);
				}
				// slurp next cmdline arguments into option arguments
				final String[] optionArgs = Arrays.copyOfRange(cmdline, index, index + parameter.getArgsCount());
				// -1, because index gets increased by one at end of for-loop
				index += parameter.getArgsCount() - 1;

				final AccessibleObject element = parameter.getElement();
				final CmdOptionHandler handler = findHandler(element, parameter.getArgsCount(),
						parameter.getCmdOptionHandlerType());

				if (handler == null) {
					throw new CmdlineParserException(null, I18n.marktr("No suitable handler found for option: {0}"),
							param);
				}

				if (!dryrun) {
					try {
						debug("Apply main parameter from parameters: {0}", Util.mkString(optionArgs, null, ", ", null));
						final boolean origAccessibleFlag = element.isAccessible();
						if (!origAccessibleFlag) {
							element.setAccessible(true);
						}
						handler.applyParams(parameter.getObject(), element, optionArgs, param);
						if (!origAccessibleFlag) {
							// do not leave doors open
							element.setAccessible(origAccessibleFlag);
						}
					} catch (final CmdOptionHandlerException e) {
						throw new CmdlineParserException(e.getMessage(), e, e.getLocalizedMessage());
					} catch (final Exception e) {
						throw new CmdlineParserException(e,
								I18n.marktr("Could not apply parameters {0} to field/method {1}"),
								Arrays.toString(optionArgs), element);
					}
				}

			} else {
				throw new CmdlineParserException(null, I18n.marktr("Unsupported option or parameter found: {0}"), param);
			}
		}

		if (!detectHelpAndSkipValidation || !helpDetected) {
			// Validate optionCount matches allowed
			for (final Entry<OptionHandle, Integer> optionC : optionCount.entrySet()) {
				final OptionHandle option = optionC.getKey();
				final Integer count = optionC.getValue();
				if (count < option.getMinCount() || (option.getMaxCount() > 0 && count > option.getMaxCount())) {
					final String rangeMsg;
					final Object[] rangeArgs;
					if (option.getMaxCount() < 0) {
						rangeMsg = I18n.marktr("at least {0}");
						rangeArgs = new Object[] { option.getMinCount() };
					} else {
						if (option.getMinCount() == option.getMaxCount()) {
							rangeMsg = I18n.marktr("exactly {0}");
							rangeArgs = new Object[] { option.getMinCount() };
						} else {
							rangeMsg = I18n.marktr("between {0} and {1}");
							rangeArgs = new Object[] { option.getMinCount(), option.getMaxCount() };
						}
					}
					final String msg;
					final Object[] msgArgs;
					final Object[] msgArgsTr;
					if (option.getNames() == null || option.getNames().length == 0) {
						msg = I18n.marktr("Main parameter \"{0}\" was given {1} times, but must be given {2} times");
						msgArgs = new Object[] { Util.mkString(option.getArgs(), null, " ", null), count,
								MessageFormat.format(rangeMsg, rangeArgs) };
						msgArgsTr = new Object[] { Util.mkString(option.getArgs(), null, " ", null), count,
								i18n.tr(rangeMsg, rangeArgs) };
					} else {
						msg = I18n.marktr("Option \"{0}\" was given {1} times, but must be given {2} times");
						msgArgs = new Object[] { option.getNames()[0], count, MessageFormat.format(rangeMsg, rangeArgs) };
						msgArgsTr = new Object[] { option.getNames()[0], count, i18n.tr(rangeMsg, rangeArgs) };
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
						// check, of an option was called with that name, if
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
								throw new CmdlineParserException(null,
										I18n.marktr("When using option \"{0}\" also option \"{1}\" must be given."),
										calledOption.getNames()[0], required);
							}
						}
					}
					for (final String conflict : calledOption.getConflictsWith()) {
						// check, of an option was called with that name, if
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
								throw new CmdlineParserException(null,
										I18n.marktr("Options \"{0}\" and \"{1}\" cannot be used at the same time."),
										calledOption.getNames()[0], conflict);
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

	protected CmdOptionHandler findHandler(final AccessibleObject element, final int argsCount,
			final Class<? extends CmdOptionHandler> cmdOptionHandlerType) {
		CmdOptionHandler handler = null;
		if (cmdOptionHandlerType != null && !cmdOptionHandlerType.equals(CmdOptionHandler.class)) {
			// requested a specific handler
			if (handlerRegistry.containsKey(cmdOptionHandlerType)) {
				handler = handlerRegistry.get(cmdOptionHandlerType);
			} else {
				try {
					handler = cmdOptionHandlerType.newInstance();
				} catch (final Exception e) {
					throw new CmdlineParserException(e, I18n.marktr("Could not create handler: {0}"),
							cmdOptionHandlerType);
				}
				// commented out, because self-introduced handler (only in a
				// specific annotation) should not be made available to all
				// other options.
				// handlerRegistry.put(annoHandlerType, handler);
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
	 * 
	 * @param objects
	 */
	public void addObject(final Object... objects) {
		for (final Object object : objects) {
			if (object.getClass().getAnnotation(CmdCommand.class) != null) {
				scanCommand(object);
			} else {
				scanOptions(object);
			}
		}
	}

	protected void scanCommand(final Object object) {
		final CmdCommand commandAnno = object.getClass().getAnnotation(CmdCommand.class);
		final String[] names = commandAnno.names();

		if (names == null || names.length == 0) {
			throw new CmdlineParserException(null, I18n.marktr("Command found without required name in: {0}"), object);
		}

		final CmdlineParser subCmdlineParser = new CmdlineParser(this, names[0], object);
		// TODO: set programm name
		final CommandHandle command = new CommandHandle(names, commandAnno.description(), subCmdlineParser, object,
				commandAnno.hidden());

		for (final String name : names) {
			if (quickCommandMap.containsKey(name) || quickOptionMap.containsKey(name)) {
				throw new CmdlineParserException(null,
						I18n.marktr("Duplicate command/option name \"{0}\" found in: {1}"), name, object);
			}
			quickCommandMap.put(name, command);
		}
		commands.add(command);

	}

	protected void validateOptions() {
		for (final OptionHandle optionHandle : options) {
			for (final String reqOptionName : optionHandle.getRequires()) {
				if (quickOptionMap.get(reqOptionName) == null) {
					// required option does not exists
					final String optionName = optionHandle.getNames() == null ? "<no name>"
							: optionHandle.getNames()[0];

					throw new CmdlineParserException(null,
							I18n.marktr("The option \"{0}\" requires the unknown/missing option \"{1}\"."), optionName,
							reqOptionName);
				}
			}
			for (final String conflictOptionName : optionHandle.getConflictsWith()) {
				final String optionName = optionHandle.getNames() == null ? "<no name>" : optionHandle.getNames()[0];
				if (Arrays.asList(optionHandle.getNames()).contains(conflictOptionName)) {
					throw new CmdlineParserException(null,
							I18n.marktr("Option \"{0}\" is configured to conflicts with itself."), optionName);
				}
				if (quickOptionMap.get(conflictOptionName) == null) {
					// required option does not exists

					throw new CmdlineParserException(null,
							I18n.marktr("The option \"{0}\" conflicts with a unknown/missing option \"{1}\"."),
							optionName, conflictOptionName);

				}
			}
		}
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

	protected void scanOptions(final Object object) {
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
			if (!pack.equals(parentClass.getPackage())) {
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

		for (final AccessibleObject element : elements) {

			if (element instanceof Field && element.getAnnotation(CmdOptionDelegate.class) != null) {
				debug("Found delegate object at: {0}", element);
				try {
					final boolean origAccessibleFlag = element.isAccessible();
					if (!origAccessibleFlag) {
						element.setAccessible(true);
					}
					final Object delegate = ((Field) element).get(object);
					if (!origAccessibleFlag) {
						// do not leave doors open
						element.setAccessible(origAccessibleFlag);
					}
					if (delegate != null) {
						scanOptions(delegate);
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
				debug("Skipping option on final field: {0}", element);
				continue;
			}

			final String[] names = anno.names();
			// The Interface itself means to specified handler
			final Class<? extends CmdOptionHandler> annoHandlerType = anno.handler() == CmdOptionHandler.class ? null
					: anno.handler();

			// No names is not allowed currently
			// TODO: check if we could use no-name options as parameter semantic
			if (names == null || names.length == 0) {
				// throw new
				// CmdlineParserException("Could not determine option name(s) for: "
				// + element);
				// No names means this is the ONLY parameter
				if (parameter != null) {
					throw new CmdlineParserException(
							null,
							I18n.marktr("More than one parameter definition found. First definition: {0} Second definition: {1}"),
							parameter.getElement(), element);
				}
				// TODO: should we ignore the help parameter?
				final OptionHandle paramHandle = new OptionHandle(new String[] {}, anno.description(), annoHandlerType,
						object, element, anno.args(), anno.minCount(), anno.maxCount(), false /*
						 * cannot
						 * be
						 * a
						 * help
						 * option
						 */, anno.hidden(),
						 anno.requires(), anno.conflictsWith());

				if (paramHandle.getArgsCount() <= 0) {
					throw new CmdlineParserException(null,
							I18n.marktr("Parameter definition must support at least on argument."));
				}
				parameter = paramHandle;

			} else {
				final OptionHandle option = new OptionHandle(names, anno.description(), annoHandlerType, object,
						element, anno.args(), anno.minCount(), anno.maxCount(), anno.isHelp(), anno.hidden(),
						anno.requires(), anno.conflictsWith());

				for (final String name : names) {
					if (quickCommandMap.containsKey(name) || quickOptionMap.containsKey(name)) {
						throw new CmdlineParserException(null,
								I18n.marktr("Duplicate command/option name \"{0}\" found in: {1}"), name, element);
					}
					quickOptionMap.put(name, option);
				}
				options.add(option);
			}
		}
	}

	public void unregisterAllHandler() {
		handlerRegistry.clear();
	}

	public void unregisterHandler(final Class<? extends CmdOptionHandler> type) {
		if (type != null) {
			handlerRegistry.remove(type);
		}
	}

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
		final StringBuilder output = new StringBuilder();
		usage(output);
		System.out.print(output.toString());
	}

	public void usage(final StringBuilder output) {
		usageFormatter.format(output, getCmdlineModel());
	}

	public CmdlineModel getCmdlineModel() {
		String programName = this.programName;
		if (parent != null) {
			// We are a command
			programName = parent.programName + " " + programName;
		}
		return new CmdlineModel(programName, options, commands, parameter, aboutLine, resourceBundle);
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
		try {
			this.resourceBundle = ResourceBundle.getBundle(resourceBundleName, Locale.getDefault(), classloader);
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

}
