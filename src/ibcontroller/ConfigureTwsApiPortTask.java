// This file is part of the "IBController".
// Copyright (C) 2004 Steven M. Kearns (skearns23@yahoo.com )
// Copyright (C) 2004 - 2011 Richard L King (rlking@aultan.com)
// For conditions of distribution and use, see copyright notice in COPYING.txt

// IBController is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.

// IBController is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.

// You should have received a copy of the GNU General Public License
// along with IBController.  If not, see <http://www.gnu.org/licenses/>.

package ibcontroller;

import java.awt.Component;
import java.awt.Container;
import javax.swing.*;

class ConfigureTwsApiPortTask implements Runnable{
    
    final int mPortNumber;
    final String mApiEnable;
    final String mApiReadOnly;
    final String mApiBypassPrecautions;
    
    ConfigureTwsApiPortTask(int portNumber, String enable, String readOnly, String bypassPrecautions) {
        mPortNumber = portNumber;
        mApiEnable = enable;
        mApiReadOnly = readOnly;
        mApiBypassPrecautions = bypassPrecautions;
    }

    @Override
    public void run() {
        try {
            final JDialog configDialog = TwsListener.getConfigDialog();    // blocks the thread until the config dialog is available
            
            GuiExecutor.instance().execute(new Runnable(){
                @Override
                public void run() {configure(configDialog, mPortNumber, mApiEnable, mApiReadOnly, mApiBypassPrecautions);}
            });

        } catch (Exception e){
            Utils.logError("" + e.getMessage());
        }
    }

    private void configure(final JDialog configDialog, final int portNumber, final String ApiEnable, final String ApiReadOnly, final String ApiBypassPrecautions) {
        try {

            final String Enable = "enable";
            final String Disable = "disable";
            final String Manual = "manual";

            if (!TwsListener.selectConfigSection(configDialog, new String[] {"API","Settings"}))
                // older versions of TWS don't have the Settings node below the API node
                TwsListener.selectConfigSection(configDialog, new String[] {"API"});

            if (portNumber != 0) {

                Utils.logToConsole("Performing port configuration");
            
                Component comp = Utils.findComponent(configDialog, "Socket port");
                if (comp == null) throw new IBControllerException("could not find socket port component");

                JTextField tf = Utils.findTextField((Container)comp, 0);
                if (tf == null) throw new IBControllerException("could not find socket port field");
            
                int currentPort = Integer.parseInt(tf.getText());
                if (currentPort == portNumber) {
                    Utils.logToConsole("TWS API socket port is already set to " + tf.getText());
                } else {
                    if (!IBController.isGateway()) {
                        JCheckBox cb = Utils.findCheckBox(configDialog, "Enable ActiveX and Socket Clients");
                        if (cb == null) throw new IBControllerException("could not find Enable ActiveX checkbox");
                        if (cb.isSelected()) TwsListener.setApiConfigChangeConfirmationExpected(true);
                    }
                    Utils.logToConsole("TWS API socket port was set to " + tf.getText());
                    tf.setText(new Integer(portNumber).toString());
                    Utils.logToConsole("TWS API socket port now set to " + tf.getText());
                }
            }

            // implement ForceTwsApiEnable=enable|disable|manual
            if (!ApiEnable.equalsIgnoreCase(Manual)) {
                Utils.logToConsole("Configure ActiveX and Socket Clients: " + ApiEnable);
                JCheckBox apicb = Utils.findCheckBox(configDialog, "Enable ActiveX and Socket Clients");
                if (apicb == null) throw new IBControllerException("could not find Enable ActiveX checkbox");
                if(ApiEnable.equalsIgnoreCase(Enable)) {
                    if (!apicb.isSelected()) apicb.doClick();
                }
                else if(ApiEnable.equalsIgnoreCase(Disable)) {
                    if (apicb.isSelected()) apicb.doClick();
                }
                Utils.logToConsole("TWS Enable ActiveX and Socket Clients checkbox was set to " + apicb.isSelected());
            }

            // implement ForceTwsApiReadOnly=enable|disable|manual
            if (!ApiReadOnly.equalsIgnoreCase(Manual)) {
                Utils.logToConsole("Configure TWS Read-Only API checkbox: " + ApiReadOnly);
                JCheckBox rocb = Utils.findCheckBox(configDialog, "Read-Only API");
                if (rocb == null) throw new IBControllerException("could not find read-only API checkbox");
                if(ApiReadOnly.equalsIgnoreCase(Enable)) {
                    if (!rocb.isSelected()) rocb.doClick();
                }
                else if(ApiReadOnly.equalsIgnoreCase(Disable)) {
                    if (rocb.isSelected()) rocb.doClick();
                }
                Utils.logToConsole("TWS Read-Only API checkbox was set to " + rocb.isSelected());
            }


            if (!ApiBypassPrecautions.equalsIgnoreCase(Manual)) {
                // now configure the Order Precautions, if the configuration calls for it 
                Utils.logToConsole("Configure Bypass Order Precautions API checkbox: " + ApiBypassPrecautions);
                if(TwsListener.selectConfigSection(configDialog, new String[] {"API","Precautions"})) {

                    JCheckBox bpcb = Utils.findCheckBox(configDialog, "Bypass Order Precautions for API Orders");
                    if (bpcb == null) throw new IBControllerException("could not find Bypass Order Precautions API checkbox");
                    if(ApiBypassPrecautions.equalsIgnoreCase(Enable)) {
                        if (!bpcb.isSelected()) bpcb.doClick();
                    }
                    else if(ApiBypassPrecautions.equalsIgnoreCase(Disable)) {
                        if (bpcb.isSelected()) bpcb.doClick();
                    }
                    Utils.logToConsole("Bypass Order Precautions for API checkbox was set to " + bpcb.isSelected());
                } else {
                    throw new IBControllerException("Can't select API->Precautions config section");
                }
            }

            Utils.clickButton(configDialog, "OK");

            configDialog.setVisible(false);
        } catch (IBControllerException e) {
            Utils.logError("" + e.getMessage());
        }
    }
}
