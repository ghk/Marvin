package com.kaulahcintaku.marvin.body;

import java.util.List;

import com.kaulahcintaku.marvin.body.command.MarvinCommand;
import com.kaulahcintaku.marvin.body.command.MarvinCommandResult;

public interface MarvinBody {
	public void shutdown();
	List<MarvinCommandResult> runCommands(List<MarvinCommand> commands);
}
