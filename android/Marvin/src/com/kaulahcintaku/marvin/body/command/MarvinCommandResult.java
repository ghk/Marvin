package com.kaulahcintaku.marvin.body.command;

public enum MarvinCommandResult {
	SUCCESS (0),

	ERR_PRO_NON_HEADER (1),
	ERR_PRO_TIMED_OUT (2),
	ERR_PRO_LENGTH_EXCEEDED (3),

	ERR_REQ_NOT_SUPPORTED (17),
	ERR_REQ_LENGTH_INVALID (18),

	ERR_CMD_INVALID (33),
	ERR_CMD_NOT_SUPPORTED (34),
	ERR_CMD_LENGTH_INVALID (35),
	ERR_CMD_TARGET_INVALID (36),
	ERR_CMD_ARG_INVALID (37)
	;

	private byte retVal;
	private MarvinCommandResult(int retVal) {
		this.retVal = (byte)retVal;
	}
	
	public static MarvinCommandResult fromRetVal(byte retVal){
		for(MarvinCommandResult item: MarvinCommandResult.values()){
			if(item.retVal == retVal)
				return item;
		}
		return null;
	}
}
