package de.tototec.cmdoption;

import static de.tobiasroeser.lambdatest.Expect.expectEquals;

import org.testng.annotations.Test;

public class MethodInheritanceTest {

	public static class PublicBaseCfg {
		private Boolean firstFlag = Boolean.FALSE;

		@CmdOption(names = { "-f1" }, args = "BOOLEAN", description = "First Flag", minCount = 0, maxCount = 1)
		public void setFirstFlag(final Boolean firstFlag) {
			this.firstFlag = firstFlag;
		}

		public Boolean getFirstFlag() {
			return firstFlag;
		}
	}

	public static class PublicChildCfg extends PublicBaseCfg {
		private Boolean secondFlag = Boolean.FALSE;

		@CmdOption(names = { "-f2" }, args = "BOOLEAN", description = "Second Flag", minCount = 0, maxCount = 1)
		public void setSecondFlag(final Boolean secondFlag) {
			this.secondFlag = secondFlag;
		}

		public Boolean getSecondFlag() {
			return secondFlag;
		}
	}

	@Test
	public void testPublicMethodInheritance() {
		final PublicChildCfg cfg = new PublicChildCfg();
		final CmdlineParser parser = new CmdlineParser(cfg);
		parser.parse(new String[] { "-f1", "TRUE", "-f2", "FALSE" });
		expectEquals(cfg.getFirstFlag(), Boolean.TRUE);
		expectEquals(cfg.getSecondFlag(), Boolean.FALSE);
	}

	public static class ProtectedBaseCfg {
		private Boolean firstFlag = Boolean.FALSE;

		@CmdOption(names = { "-f1" }, args = "BOOLEAN", description = "First Flag", minCount = 0, maxCount = 1)
		protected void setFirstFlag(final Boolean firstFlag) {
			this.firstFlag = firstFlag;
		}

		public Boolean getFirstFlag() {
			return firstFlag;
		}
	}

	public static class ProtectedChildCfg extends ProtectedBaseCfg {
		private Boolean secondFlag = Boolean.FALSE;

		@CmdOption(names = { "-f2" }, args = "BOOLEAN", description = "Second Flag", minCount = 0, maxCount = 1)
		protected void setSecondFlag(final Boolean secondFlag) {
			this.secondFlag = secondFlag;
		}

		public Boolean getSecondFlag() {
			return secondFlag;
		}
	}

	@Test
	public void testProtectedMethodInheritance() {
		final ProtectedChildCfg cfg = new ProtectedChildCfg();
		final CmdlineParser parser = new CmdlineParser(cfg);
		parser.parse(new String[] { "-f1", "TRUE", "-f2", "FALSE" });
		expectEquals(cfg.getFirstFlag(), Boolean.TRUE);
		expectEquals(cfg.getSecondFlag(), Boolean.FALSE);
	}

	public static class PackagePrivateBaseCfg {
		private Boolean firstFlag = Boolean.FALSE;

		@CmdOption(names = { "-f1" }, args = "BOOLEAN", description = "First Flag", minCount = 0, maxCount = 1)
		private void setFirstFlag(final Boolean firstFlag) {
			this.firstFlag = firstFlag;
		}

		public Boolean getFirstFlag() {
			return firstFlag;
		}
	}

	public static class PackagePrivateChildCfg extends PackagePrivateBaseCfg {
		private Boolean secondFlag = Boolean.FALSE;

		@CmdOption(names = { "-f2" }, args = "BOOLEAN", description = "Second Flag", minCount = 0, maxCount = 1)
		private void setSecondFlag(final Boolean secondFlag) {
			this.secondFlag = secondFlag;
		}

		public Boolean getSecondFlag() {
			return secondFlag;
		}
	}

	@Test
	public void testPackagePrivateMethodInheritance() {
		final PackagePrivateChildCfg cfg = new PackagePrivateChildCfg();
		final CmdlineParser parser = new CmdlineParser(cfg);
		parser.parse(new String[] { "-f1", "TRUE", "-f2", "FALSE" });
		expectEquals(cfg.getFirstFlag(), Boolean.TRUE);
		expectEquals(cfg.getSecondFlag(), Boolean.FALSE);
	}

	public static class PrivateBaseCfg {
		private Boolean firstFlag = Boolean.FALSE;

		@CmdOption(names = { "-f1" }, args = "BOOLEAN", description = "First Flag", minCount = 0, maxCount = 1)
		private void setFirstFlag(final Boolean firstFlag) {
			this.firstFlag = firstFlag;
		}

		public Boolean getFirstFlag() {
			return firstFlag;
		}
	}

	public static class PrivateChildCfg extends PrivateBaseCfg {
		private Boolean secondFlag = Boolean.FALSE;

		@CmdOption(names = { "-f2" }, args = "BOOLEAN", description = "Second Flag", minCount = 0, maxCount = 1)
		private void setSecondFlag(final Boolean secondFlag) {
			this.secondFlag = secondFlag;
		}

		public Boolean getSecondFlag() {
			return secondFlag;
		}
	}

	@Test
	public void testPrivateMethodInheritance() {
		final PrivateChildCfg cfg = new PrivateChildCfg();
		final CmdlineParser parser = new CmdlineParser(cfg);
		parser.parse(new String[] { "-f1", "TRUE", "-f2", "FALSE" });
		expectEquals(cfg.getFirstFlag(), Boolean.TRUE);
		expectEquals(cfg.getSecondFlag(), Boolean.FALSE);
	}

}
