package de.tototec.cmdoption.handler;

import static de.tobiasroeser.lambdatest.Expect.expectEquals;

import de.tobiasroeser.lambdatest.testng.FreeSpec;
import de.tototec.cmdoption.CmdOption;
import de.tototec.cmdoption.CmdlineParser;
import de.tototec.cmdoption.CmdlineParserException;
import de.tototec.cmdoption.internal.FList;

public class EnumHandlerTest extends FreeSpec {

	public static enum MyEnum {
		V1, V2, V3
	}

	public static class ConfigField {
		@CmdOption(names = "--my-enum", args = "ENUM")
		public MyEnum myEnum;
	}

	public static class ConfigMethod {
		private MyEnum myEnum;

		@CmdOption(names = "--my-enum", args = "ENUM")
		public void setMyEnum(final MyEnum myEnum) {
			this.myEnum = myEnum;
		}

		public MyEnum getMyEnum() {
			return myEnum;
		}
	}

	public CmdlineParser cp() {
		final CmdlineParser cp = new CmdlineParser();
		cp.unregisterAllHandler();
		cp.registerHandler(new EnumHandler());
		return cp;
	}

	{
		FList.foreach(MyEnum.values(), e -> {
			test("Parse enum field " + e, () -> {
				final ConfigField config = new ConfigField();
				final CmdlineParser cp = cp();
				cp.addObject(config);
				cp.parse("--my-enum", e.name());
				expectEquals(config.myEnum, e);
			});
			test("Parse enum method " + e, () -> {
				final ConfigMethod config = new ConfigMethod();
				final CmdlineParser cp = cp();
				cp.addObject(config);
				cp.parse("--my-enum", e.name());
				expectEquals(config.getMyEnum(), e);
			});
		});

		FList.foreach(new String[] { "", "V", "V4", "v1" }, e -> {
			test("Enum field failed with invalid value: " + e, () -> {
				final ConfigField config = new ConfigField();
				final CmdlineParser cp = cp();
				cp.addObject(config);
				intercept(CmdlineParserException.class,
						"Invalid enum value: \"" + e + "\". Supported values: V1, V2, V3", () -> {
							cp.parse("--my-enum", e);
						});
			});
			test("Enum method failed with invalid value: " + e, () -> {
				final ConfigMethod config = new ConfigMethod();
				final CmdlineParser cp = cp();
				cp.addObject(config);
				intercept(CmdlineParserException.class,
						"Invalid enum value: \"" + e + "\". Supported values: V1, V2, V3", () -> {
							cp.parse("--my-enum", e);
						});
			});
		});

	}

}
