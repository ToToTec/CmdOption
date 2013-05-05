package de.tototec.cmdoption;

import org.testng.Assert;
import org.testng.annotations.Test;

public class MethodInheritanceTest extends Assert {

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
		assertEquals(cfg.getFirstFlag(), Boolean.TRUE);
		assertEquals(cfg.getSecondFlag(), Boolean.FALSE);
	}

}
