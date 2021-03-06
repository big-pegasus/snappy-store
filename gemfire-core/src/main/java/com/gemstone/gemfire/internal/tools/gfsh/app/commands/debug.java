/*
 * Copyright (c) 2010-2015 Pivotal Software, Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You
 * may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License. See accompanying
 * LICENSE file.
 */
package com.gemstone.gemfire.internal.tools.gfsh.app.commands;

import java.util.ArrayList;

import com.gemstone.gemfire.internal.tools.gfsh.app.CommandExecutable;
import com.gemstone.gemfire.internal.tools.gfsh.app.Gfsh;

public class debug implements CommandExecutable {
	private Gfsh gfsh;

	public debug(Gfsh gfsh) {
		this.gfsh = gfsh;
	}

	public void help() {
		gfsh.println("debug [true|false] | [-?]");
		gfsh.println("     Toggle the debug setting. If debug is true then execptions");
		gfsh.println("     are printed to stdout.");
		gfsh.println();
	}

	public void execute(String command) throws Exception {
		if (command.startsWith("debug -?")) {
			help();
		} else {
			debug(command);
		}
	}

	private void debug(String command) {
		ArrayList<String> list = new ArrayList();
		gfsh.parseCommand(command, list);
		if (list.size() >= 2) {
			if (list.get(1).equalsIgnoreCase("true")
					|| list.get(1).equalsIgnoreCase("false")) {
				boolean enable = list.get(1).equalsIgnoreCase("true");
				gfsh.setDebug(enable);
			} else {
				gfsh.println("Invalid option:" + list.get(1));
				gfsh.println("Check help for valid options.");
				return;
			}
		} else {
			gfsh.setDebug(!gfsh.isDebug());
		}
		gfsh.println("debug is " + (gfsh.isDebug() ? "on" : "off"));
	}
}
