/*
 * Copyright (c) 2008, SQL Power Group Inc.
 *
 * This file is part of DQguru
 *
 * DQguru is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DQguru is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */


package ca.sqlpower.matchmaker;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import junit.framework.TestCase;
import ca.sqlpower.sql.PLSchemaException;
import ca.sqlpower.sqlobject.SQLDatabase;

public class MatchMakerEngineImplTest extends TestCase {

	Project project;
	MatchEngineImpl matchMakerEngine;
	private TestingMatchMakerSession session;
	private MatchMakerSessionContext context;
	
	protected void setUp() throws Exception {
		super.setUp();
		project = new Project();
		session = new TestingMatchMakerSession();
		session.setDatabase(new SQLDatabase());
		project.setSession(session);
		matchMakerEngine = new MatchEngineImpl(session,project);
		context = session.getContext();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testValidateEmailSettingsValid() throws SQLException, PLSchemaException{
		context.setEmailSmtpHost("mail.sqlpower.ca");
		assertTrue(matchMakerEngine.validateEmailSetting(context));
	}
	
	public void testValidateEmailSettingsNoHost() throws SQLException, PLSchemaException{
		context.setEmailSmtpHost("");
		assertFalse("The email is valid without a smtp host address", matchMakerEngine.validateEmailSetting(context));
	}

	//   check for unwriteable file
    public void testLogCantWrite() throws IOException{
        MatchMakerSettings settings = new MungeSettings(); 
        File log = new File("mmenginetest.log");
        settings.setLog(log);
        assertTrue(log.createNewFile());
        assertTrue(log.setReadOnly());
        assertFalse(log.canWrite());
        assertFalse(AbstractEngine.canWriteLogFile(settings));
        log.delete();
    }
    
    // check for append
    public void testCanWriteLogExists() throws IOException{
        MatchMakerSettings settings = new MungeSettings(); 
        File log = new File("mmenginetest.log");
        settings.setLog(log);
        log.createNewFile();
        assertTrue(log.canWrite());
        assertTrue(AbstractEngine.canWriteLogFile(settings));
        log.delete();
    }
    
    // check for create
    public void testCanWriteLogNonExistantButWritable() throws IOException{
        MatchMakerSettings settings = new MungeSettings(); 
        File log = new File("mmenginetest.log");
        settings.setLog(log);
        log.createNewFile();
        assertTrue(log.canWrite());
        log.delete();
        assertTrue(AbstractEngine.canWriteLogFile(settings));
    }
    
    
//  check for append
    public void testCanReadLogExists() throws IOException{
        MatchMakerSettings settings = new MungeSettings(); 
        File log = new File("mmenginetest.log");
        settings.setLog(log);
        log.createNewFile();
        assertTrue(log.canWrite());
        assertTrue(AbstractEngine.canReadLogFile(settings));
        log.delete();
    }
    
    // check for create
    public void testCanReadLogNonExistant() throws IOException{
        MatchMakerSettings settings = new MungeSettings(); 
        File log = new File("mmenginetest.log");
        settings.setLog(log);
        // this is an unreadable file (I hope)
        log.mkdir();
        assertTrue(AbstractEngine.canReadLogFile(settings));
        log.delete();
    }
    

}
