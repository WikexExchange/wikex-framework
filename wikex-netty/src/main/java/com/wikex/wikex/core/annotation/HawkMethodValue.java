/**
 * Copyright (c) 2016-2017  All Rights Reserved.
 * 
 * <p>FileName: Hawk.java</p>
 * 
 * Description: Holds the values associated with a Hawk method annotation.
 * 
 * @author MrGao
 * @date July 18, 2019
 * @version 1.0
 * History:
 * v1.0.0, July 18, 2019, Create
 */
package com.wikex.wikex.core.annotation;

/**
 * <p>Title: HawkMethodValue</p>
 * <p>Description: Encapsulates the command number, version, and expiration status
 * for a service method annotated with @HawkMethod.</p>
 * 
 * This class stores the metadata such as:
 * - Command number
 * - Version
 * - Whether the service method is considered obsolete
 * 
 * By default, methods are not obsolete.
 * 
 * @author MrGao
 * @date July 18, 2019
 */
public class HawkMethodValue {
    private int cmd;
    private byte version;
    /**
     * Whether the service method is obsolete; defaults to not obsolete.
     */
    private boolean obsoleted;

    public HawkMethodValue() {
    }

    public HawkMethodValue(int cmd, byte version, boolean obsoleted) {
        this.cmd = cmd;
        this.version = version;
        this.obsoleted = obsoleted;
    }

    public int getCmd() {
        return cmd;
    }

    public void setCmd(int cmd) {
        this.cmd = cmd;
    }

    public byte getVersion() {
        return version;
    }

    public void setVersion(byte version) {
        this.version = version;
    }

    public boolean isObsoleted() {
        return obsoleted;
    }

    public void setObsoleted(boolean obsoleted) {
        this.obsoleted = obsoleted;
    }
}
