package de.tototec.cmdoption;

import org.testng.Assert;
import org.testng.annotations.Test;

public class FieldInheritanceTest extends Assert {

	public static class PublicBaseCfg {
		@CmdOption(names = { "-f1" }, args = "BOOLEAN", description = "First Flag", minCount = 0, maxCount = 1)
		public Boolean firstFlag = Boolean.FALSE;
	}

	public static class PublicChildCfg extends PublicBaseCfg {
		@CmdOption(names = { "-f2" }, args = "BOOLEAN", description = "Second Flag", minCount = 0, maxCount = 1)
		public Boolean secondFlag = Boolean.FALSE;
	}

	@Test
	public void testInheritancePublicFields() {
		final PublicChildCfg cfg = new PublicChildCfg();
		final CmdlineParser parser = new CmdlineParser(cfg);
		parser.parse(new String[] { "-f1", "TRUE", "-f2", "FALSE" });
		assertEquals(cfg.firstFlag, Boolean.TRUE);
		assertEquals(cfg.secondFlag, Boolean.FALSE);
	}

	public static class ProtectedBaseCfg {
		@CmdOption(names = { "-f1" }, args = "BOOLEAN", description = "First Flag", minCount = 0, maxCount = 1)
		protected Boolean firstFlag = Boolean.FALSE;
	}

	public static class ProtectedChildCfg extends ProtectedBaseCfg {
		@CmdOption(names = { "-f2" }, args = "BOOLEAN", description = "Second Flag", minCount = 0, maxCount = 1)
		protected Boolean secondFlag = Boolean.FALSE;
	}

	@Test
	public void testInheritanceProtectedFields() {
		final ProtectedChildCfg cfg = new ProtectedChildCfg();
		final CmdlineParser parser = new CmdlineParser(cfg);
		parser.parse(new String[] { "-f1", "TRUE", "-f2", "FALSE" });
		assertEquals(cfg.firstFlag, Boolean.TRUE);
		assertEquals(cfg.secondFlag, Boolean.FALSE);
	}

	public static class PackagePrivateBaseCfg {
		@CmdOption(names = { "-f1" }, args = "BOOLEAN", description = "First Flag", minCount = 0, maxCount = 1)
		Boolean firstFlag = Boolean.FALSE;
	}

	public static class PackagePrivateChildCfg extends PackagePrivateBaseCfg {
		@CmdOption(names = { "-f2" }, args = "BOOLEAN", description = "Second Flag", minCount = 0, maxCount = 1)
		Boolean secondFlag = Boolean.FALSE;
	}

	@Test
	public void testInheritancePackagePrivateFields() {
		final PackagePrivateChildCfg cfg = new PackagePrivateChildCfg();
		final CmdlineParser parser = new CmdlineParser(cfg);
		parser.parse(new String[] { "-f1", "TRUE", "-f2", "FALSE" });
		assertEquals(cfg.firstFlag, Boolean.TRUE);
		assertEquals(cfg.secondFlag, Boolean.FALSE);
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

	@Test
	public void testInheritancePrivateFields() {
		final PrivateChildCfg cfg = new PrivateChildCfg();
		final CmdlineParser parser = new CmdlineParser(cfg);
		parser.parse(new String[] { "-f1", "TRUE", "-f2", "FALSE" });
		assertEquals(cfg.getFirstFlag(), Boolean.TRUE);
		assertEquals(cfg.secondFlag, Boolean.FALSE);
	}

}
