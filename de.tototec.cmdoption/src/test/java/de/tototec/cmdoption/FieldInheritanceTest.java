package de.tototec.cmdoption;

import static de.tobiasroeser.lambdatest.Expect.expectEquals;

import de.tobiasroeser.lambdatest.junit.FreeSpec;

public class FieldInheritanceTest extends FreeSpec {

	public static class PublicBaseCfg {
		@CmdOption(names = { "-f1" }, args = "BOOLEAN", description = "First Flag", minCount = 0, maxCount = 1)
		public Boolean firstFlag = Boolean.FALSE;
	}

	public static class PublicChildCfg extends PublicBaseCfg {
		@CmdOption(names = { "-f2" }, args = "BOOLEAN", description = "Second Flag", minCount = 0, maxCount = 1)
		public Boolean secondFlag = Boolean.FALSE;
	}

	public static class ProtectedBaseCfg {
		@CmdOption(names = { "-f1" }, args = "BOOLEAN", description = "First Flag", minCount = 0, maxCount = 1)
		protected Boolean firstFlag = Boolean.FALSE;
	}

	public static class ProtectedChildCfg extends ProtectedBaseCfg {
		@CmdOption(names = { "-f2" }, args = "BOOLEAN", description = "Second Flag", minCount = 0, maxCount = 1)
		protected Boolean secondFlag = Boolean.FALSE;
	}

	public static class PackagePrivateBaseCfg {
		@CmdOption(names = { "-f1" }, args = "BOOLEAN", description = "First Flag", minCount = 0, maxCount = 1)
		Boolean firstFlag = Boolean.FALSE;
	}

	public static class PackagePrivateChildCfg extends PackagePrivateBaseCfg {
		@CmdOption(names = { "-f2" }, args = "BOOLEAN", description = "Second Flag", minCount = 0, maxCount = 1)
		Boolean secondFlag = Boolean.FALSE;
	}

	public static class PrivateBaseCfg {
		@CmdOption(names = { "-f1" }, args = "BOOLEAN", description = "First Flag", minCount = 0, maxCount = 1)
		private Boolean firstFlag = Boolean.FALSE;

		public Boolean getFirstFlag() {
			return firstFlag;
		}
	}

	public static class PrivateChildCfg extends PrivateBaseCfg {
		@CmdOption(names = { "-f2" }, args = "BOOLEAN", description = "Second Flag", minCount = 0, maxCount = 1)
		private Boolean secondFlag = Boolean.FALSE;
	}

	public FieldInheritanceTest() {
		test("Inheritance of public fields", () -> {
			final PublicChildCfg cfg = new PublicChildCfg();
			final CmdlineParser parser = new CmdlineParser(cfg);
			parser.parse(new String[] { "-f1", "TRUE", "-f2", "FALSE" });
			expectEquals(cfg.firstFlag, Boolean.TRUE);
			expectEquals(cfg.secondFlag, Boolean.FALSE);
		});

		test("Inheritance of protected fields", () -> {
			final ProtectedChildCfg cfg = new ProtectedChildCfg();
			final CmdlineParser parser = new CmdlineParser(cfg);
			parser.parse(new String[] { "-f1", "TRUE", "-f2", "FALSE" });
			expectEquals(cfg.firstFlag, Boolean.TRUE);
			expectEquals(cfg.secondFlag, Boolean.FALSE);
		});

		test("Inheritance of package private fields", () -> {
			final PackagePrivateChildCfg cfg = new PackagePrivateChildCfg();
			final CmdlineParser parser = new CmdlineParser(cfg);
			parser.parse(new String[] { "-f1", "TRUE", "-f2", "FALSE" });
			expectEquals(cfg.firstFlag, Boolean.TRUE);
			expectEquals(cfg.secondFlag, Boolean.FALSE);
		});

		test("Inheritance of private fields", () -> {
			final PrivateChildCfg cfg = new PrivateChildCfg();
			final CmdlineParser parser = new CmdlineParser(cfg);
			parser.parse(new String[] { "-f1", "TRUE", "-f2", "FALSE" });
			expectEquals(cfg.getFirstFlag(), Boolean.TRUE);
			expectEquals(cfg.secondFlag, Boolean.FALSE);
		});
	}
}
