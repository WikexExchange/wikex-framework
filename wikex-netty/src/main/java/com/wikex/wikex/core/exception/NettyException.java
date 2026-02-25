
package com.wikex.wikex.core.exception;


import com.wikex.wikex.core.common.constant.NettyResponseBean;

/**
 * <p>Title: NettyException</p>
 * <p>Description: </p>
 * @author MrGao
 * @date 
 */
public class NettyException extends BaseException {
	/** 
     * Constructors 
     *
     * @param code
     *            
     */  
    public NettyException(String code) {
        super(code, null, code, null);  
    }  

    public NettyException(NettyResponseBean responseBean){
        super(responseBean.getResponseString(),null,
                responseBean.getResponseString(),null  );
    }
    /** 
     * Constructors 
     *  
     * @param cause 
     *             
     * @param code 
     *             
     */  
    public NettyException(Throwable cause, String code) {
        super(code, cause, code, null);  
    }  
  
    /** 
     * Constructors 
     *  
     * @param code 
     *             
     * @param values 
     *             
     */  
    public NettyException(String code, Object[] values) {
        super(code, null, code, values);  
    }  
  
    /** 
     * Constructors 
     *  
     * @param cause 
     *             
     * @param code 
     *             
     * @param values 
     *             
     */  
    public NettyException(Throwable cause, String code, Object[] values) {
        super(code, null, code, values);  
    }  
  
    private static final long serialVersionUID = -3711290613973933714L;  
}
