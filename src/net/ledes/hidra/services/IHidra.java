package net.ledes.hidra.services;

import javax.jws.WebMethod;
import javax.jws.WebParam;

public interface IHidra {

    @WebMethod
    public boolean start(@WebParam(name = "localPath") String localPath);
    
    @WebMethod
    public boolean addOn(@WebParam(name = "fileName") String fileName);
    
    @WebMethod
    public boolean remove(@WebParam(name = "filename") String filename);
    
    @WebMethod
    public boolean commit(@WebParam(name = "message") String message);
    
    @WebMethod
    public boolean clone(@WebParam(name = "remotePath") String remotePath, @WebParam(name = "localPath") String localPath);
    
    @WebMethod
    public boolean status();
    
    @WebMethod
    public boolean Logs();
    
    @WebMethod
    public boolean Branch();
    
    @WebMethod
    public boolean createBranch(@WebParam(name = "nameBranch")String nameBranch);
}
