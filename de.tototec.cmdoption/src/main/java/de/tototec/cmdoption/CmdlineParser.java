package de.tototec.cmdoption;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import de.tototec.cmdoption.handler.AddToCollectionHandler;
import de.tototec.cmdoption.handler.BooleanFieldHandler;
import de.tototec.cmdoption.handler.BooleanOptionHandler;
import de.tototec.cmdoption.handler.CmdOptionHandler;
import de.tototec.cmdoption.handler.CmdOptionHandlerException;
import de.tototec.cmdoption.handler.PutIntoMapHandler;
import de.tototec.cmdoption.handler.StringFieldHandler;
import de.tototec.cmdoption.handler.StringMethodHandler;

public class CmdlineParser {

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

	private boolean debugMode = false;
	private final CmdlineParser parent;

	protected CmdlineParser(CmdlineParser parent, String commandName, Object commandObject) {
		this.parent = parent;
		debugMode = parent.debugMode;
		programName = commandName;
		handlerRegistry = parent.handlerRegistry;
		scanOptions(commandObject);
	}

	public CmdlineParser(Object... objects) {
		parent = null;
		programName = "<main class>";
		handlerRegistry = new LinkedHashMap<Class<? extends CmdOptionHandler>, CmdOptionHandler>();

		addObject(objects);

		registerHandler(new BooleanOptionHandler());
		registerHandler(new BooleanFieldHandler());
		registerHandler(new StringFieldHandler());
		registerHandler(new PutIntoMapHandler());
		registerHandler(new AddToCollectionHandler());
		registerHandler(new StringMethodHandler());
	}

	private void debug(String msg, Object... args) {
		if (parent != null) {
			parent.debug(msg, args);
		} else {
			if (debugMode) {
				if (args == null || args.length == 0) {
					System.out.println("CMDOPTION: " + msg);
				} else {
					System.out.println("CMDOPTION: " + MessageFormat.format(msg, args));
				}
			}
		}
	}

	public void setUsageFormatter(UsageFormatter usageFormatter) {
		this.usageFormatter = usageFormatter;
	}

	// TODO: enable some kind of help-scanner. It should not validate the
	// commandline
	// public void parseHelp(String... cmdline) {
	//
	// }

	public void setDefaultCommandName(String defaultCommandName) {
		this.defaultCommandName = defaultCommandName;
	}

	public void setDefaultCommandClass(Class<?> defaultCommandClass) {
		CmdCommand anno = defaultCommandClass.getAnnotation(CmdCommand.class);
		if (anno == null) {
			throw new IllegalArgumentException("Given class is not annotated with @" + CmdCommand.class.getSimpleName());
		}
		if (anno.names() == null || anno.names().length == 0 || anno.names()[0].length() == 0) {
			throw new IllegalArgumentException("Given default command class has no valid name");
		}
		setDefaultCommandName(anno.names()[0]);
	}

	public void parse(String... cmdline) {
		parse(false, true, cmdline);
	}

	public void parse(boolean dryrun, boolean detectHelpAndSkipValidation, String... cmdline) {
		if (defaultCommandName != null && !quickCommandMap.containsKey(defaultCommandName)) {
			throw new CmdlineParserException("Default command '" + defaultCommandName + "' is not a known command.");
		}

		if (!dryrun) {
			// Check without applying anything
			parse(true, detectHelpAndSkipValidation, cmdline);
		}
		// Avoid null access
		cmdline = cmdline == null ? new String[] {} : cmdline;

		// Should be set to false, if an stopOption was found and parsing of
		// options is no longer allowed
		boolean parseOptions = true;
		final String stopOption = "--";

		// optionCount counts the occurence for each option handle in the
		// cmdline
		final Map<OptionHandle, Integer> optionCount = new LinkedHashMap<OptionHandle, Integer>();
		for (OptionHandle option : options) {
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

			} else if (param.equals("--CMDOPTION_DEBUG")) {
				debugMode = true;
				debug("Enabled debug mode.");

			} else if (parseOptions && quickOptionMap.containsKey(param)) {
				// Found an option
				OptionHandle optionHandle = quickOptionMap.get(param);
				optionCount.put(optionHandle, optionCount.get(optionHandle) + 1);
				if (optionHandle.isHelp()) {
					helpDetected = true;
				}

				if (cmdline.length <= index + optionHandle.getArgsCount()) {
					int countOfGivenParams = cmdline.length - index - 1;
					throw new CmdlineParserException("Missing argument(s): "
							+ Util.mkString(
									Arrays.asList(optionHandle.getArgs()).subList(countOfGivenParams,
											optionHandle.getArgsCount()), null, ", ", null) + ". Option '" + param
							+ "' requires " + optionHandle.getArgsCount() + " arguments, but you gave "
							+ countOfGivenParams + ".");
				}
				// slurp next cmdline arguments into option arguments
				String[] optionArgs = Arrays.copyOfRange(cmdline, index + 1, index + 1 + optionHandle.getArgsCount());
				index += optionHandle.getArgsCount();

				AccessibleObject element = optionHandle.getElement();
				CmdOptionHandler handler = findHandler(element, optionHandle.getArgsCount(),
						optionHandle.getCmdOptionHandlerType());

				if (handler == null) {
					throw new CmdlineParserException("No suitable handler found for option: " + param + " ("
							+ optionHandle.getArgsCount() + " argument(s))");
				}

				if (!dryrun) {
					try {
						handler.applyParams(optionHandle.getObject(), element, optionArgs);
					} catch (CmdOptionHandlerException e) {
						throw new CmdlineParserException(e.getMessage(), e);
					} catch (Exception e) {
						throw new CmdlineParserException("Could not apply parameters " + Arrays.toString(optionArgs)
								+ " to field/method " + element, e);
					}
				}
			} else if (parseOptions && quickCommandMap.containsKey(param)) {
				// Found a command
				CommandHandle commandHandle = quickCommandMap.get(param);
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
				// Asume a default command inserted here
				debug("Unsupported option '" + param + "' found, assuming default command: " + defaultCommandName);
				CommandHandle commandHandle = quickCommandMap.get(defaultCommandName);

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
					int countOfGivenParams = cmdline.length - index;
					throw new CmdlineParserException("Missing arguments: "
							+ Arrays.asList(parameter.getArgs()).subList(countOfGivenParams, parameter.getArgsCount())
							+ " Parameter requires " + parameter.getArgsCount() + " arguments, but you gave "
							+ countOfGivenParams + ".");
				}
				// slurp next cmdline arguments into option arguments
				String[] optionArgs = Arrays.copyOfRange(cmdline, index, index + parameter.getArgsCount());
				// -1, because index gets increased by one at end of for-loop
				index += parameter.getArgsCount() - 1;

				AccessibleObject element = parameter.getElement();
				CmdOptionHandler handler = findHandler(element, parameter.getArgsCount(),
						parameter.getCmdOptionHandlerType());

				if (handler == null) {
					throw new CmdlineParserException("No suitable handler found for option: " + param);
				}

				if (!dryrun) {
					try {
						debug("Apply main parameter from parameters: {0}", Util.mkString(optionArgs, null, ", ", null));
						handler.applyParams(parameter.getObject(), element, optionArgs);
					} catch (CmdOptionHandlerException e) {
						throw new CmdlineParserException(e.getMessage(), e);
					} catch (Exception e) {
						throw new CmdlineParserException("Could not apply parameters " + Arrays.toString(optionArgs)
								+ " to field/method " + element, e);
					}
				}

			} else {
				throw new CmdlineParserException("Unsupported option found: " + param);
			}
		}

		if (!detectHelpAndSkipValidation || !helpDetected) {
			// Validate optionCount matches allowed
			for (Entry<OptionHandle, Integer> optionC : optionCount.entrySet()) {
				OptionHandle option = optionC.getKey();
				Integer count = optionC.getValue();
				if (count < option.getMinCount() || (option.getMaxCount() > 0 && count > option.getMaxCount())) {
					final String range;
					if (option.getMaxCount() < 0) {
						range = "at least " + option.getMinCount() + " times";
					} else {
						range = "between " + option.getMinCount() + " and " + option.getMaxCount() + " times";
					}
					final String optionName;
					if (option.getNames() == null || option.getNames().length == 0) {
						optionName = "Main parameter '" + Util.mkString(option.getArgs(), null, " ", null) + "'";
					} else {
						optionName = "Option '" + option.getNames()[0] + "'";
					}
					throw new CmdlineParserException(optionName + " was given " + count + " times, but must be given "
							+ range);
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

	protected CmdOptionHandler findHandler(AccessibleObject element, int argsCount,
			Class<? extends CmdOptionHandler> cmdOptionHandlerType) {
		CmdOptionHandler handler = null;
		if (cmdOptionHandlerType != null && !cmdOptionHandlerType.equals(CmdOptionHandler.class)) {
			// requested a specific handler
			if (handlerRegistry.containsKey(cmdOptionHandlerType)) {
				handler = handlerRegistry.get(cmdOptionHandlerType);
			} else {
				try {
					handler = cmdOptionHandlerType.newInstance();
				} catch (Exception e) {
					throw new CmdlineParserException("Could not create handler: " + cmdOptionHandlerType, e);
				}
				// commented out, because self-introduced handler (only in a
				// specific annotation) should not be made available to all
				// other options.
				// handlerRegistry.put(annoHandlerType, handler);
			}
		} else {
			// walk through registered hander and find one
			for (CmdOptionHandler regHandle : handlerRegistry.values()) {
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

	public void addObject(Object... objects) {
		for (Object object : objects) {
			if (object.getClass().getAnnotation(CmdCommand.class) != null) {
				scanCommand(object);
			} else {
				scanOptions(object);
			}
		}
	}

	protected void scanCommand(Object object) {
		CmdCommand commandAnno = object.getClass().getAnnotation(CmdCommand.class);
		String[] names = commandAnno.names();

		if (names == null || names.length == 0) {
			throw new CmdlineParserException("Command found without required name in: " + object);
		}

		CmdlineParser subCmdlineParser = new CmdlineParser(this, names[0], object);
		// TODO: set programm name
		CommandHandle command = new CommandHandle(names, commandAnno.description(), subCmdlineParser, object);

		for (String name : names) {
			if (quickCommandMap.containsKey(name) || quickOptionMap.containsKey(name)) {
				throw new CmdlineParserException("Duplicate command/option name '" + name + "' found in: " + object);
			}
			quickCommandMap.put(name, command);
		}
		commands.add(command);

	}

	protected void scanOptions(Object object) {
		Class<?> class1 = object.getClass();

		Set<AccessibleObject> elements = new LinkedHashSet<AccessibleObject>();
		elements.addAll(Arrays.asList(class1.getDeclaredFields()));
		elements.addAll(Arrays.asList(class1.getFields()));
		elements.addAll(Arrays.asList(class1.getDeclaredMethods()));
		elements.addAll(Arrays.asList(class1.getMethods()));

		for (AccessibleObject element : elements) {
			element.setAccessible(true);

			if (element instanceof Field && element.getAnnotation(CmdOptionDelegate.class) != null) {
				debug("Found delegate object at: {0}", element);
				try {
					Object delegate = ((Field) element).get(object);
					if (delegate != null) {
						scanOptions(delegate);
					}
				} catch (IllegalArgumentException e) {
					debug("Could not scan delegate object at: {0}", element);
				} catch (IllegalAccessException e) {
					debug("Could not scan delegate object at: {0}", element);
				}
				continue;
			}

			CmdOption anno = element.getAnnotation(CmdOption.class);
			if (anno == null) {
				continue;
			}

			String[] names = anno.names();
			// The Interface itself means to specified handler
			Class<? extends CmdOptionHandler> annoHandlerType = anno.handler() == CmdOptionHandler.class ? null : anno
					.handler();

			// No names is not allowed currently
			// TODO: check if we could use no-name options as parameter semantic
			if (names == null || names.length == 0) {
				// throw new
				// CmdlineParserException("Could not determine option name(s) for: "
				// + element);
				// No names means this is the ONLY parameter
				if (parameter != null) {
					throw new CmdlineParserException("More than one parameter definition found. First definition: "
							+ parameter.getElement() + " Second definition: " + element);
				}
				// TODO: should we ignore the help parameter?
				OptionHandle paramHandle = new OptionHandle(new String[] {}, anno.description(), annoHandlerType,
						object, element, anno.args(), anno.minCount(), anno.maxCount(), false /*
																							 * cannot
																							 * be
																							 * a
																							 * help
																							 * option
																							 */, anno.hidden());

				if (paramHandle.getArgsCount() <= 0) {
					throw new CmdlineParserException("Parameter definition must support at least on argument.");
				}
				parameter = paramHandle;

			} else {
				OptionHandle option = new OptionHandle(names, anno.description(), annoHandlerType, object, element,
						anno.args(), anno.minCount(), anno.maxCount(), anno.isHelp(), anno.hidden());

				for (String name : names) {
					if (quickCommandMap.containsKey(name) || quickOptionMap.containsKey(name)) {
						throw new CmdlineParserException("Duplicate command/option name '" + name + "' found in: "
								+ element);
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

	public void unregisterHandler(Class<? extends CmdOptionHandler> type) {
		if (type != null) {
			handlerRegistry.remove(type);
		}
	}

	public void registerHandler(CmdOptionHandler handler) {
		if (handler != null) {
			debug("Register CmdOptionHandler: {0}", handler);
			handlerRegistry.put(handler.getClass(), handler);
		}
	}

	public void commandUsage(Class<?> command) {
		for (CommandHandle cmdHandle : commands) {
			if (cmdHandle.getObject().getClass().equals(command)) {
				cmdHandle.getCmdlineParser().usage();
				return;
			}
		}

		throw new IllegalArgumentException("Given command is not known or does not have a @"
				+ CmdCommand.class.getSimpleName() + "-annotation");
	}

	public void usage() {
		StringBuilder output = new StringBuilder();
		usage(output);
		System.out.print(output.toString());
	}

	public void usage(StringBuilder output) {
		String programName = this.programName;
		if (parent != null) {
			// We are a command
			programName = parent.programName + " " + programName;
		}
		usageFormatter.format(output, programName, options, commands, parameter);
	}

	public List<OptionHandle> getOptions() {
		return options;
	}

	public List<CommandHandle> getCommands() {
		return commands;
	}

	public OptionHandle getParameter() {
		return parameter;
	}

	public String getProgramName() {
		return programName;
	}

	public void setProgramName(String programName) {
		this.programName = programName;
	}

}
