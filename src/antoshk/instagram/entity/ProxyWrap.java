package antoshk.instagram.entity;

import java.net.Proxy;
import java.net.SocketAddress;

public class ProxyWrap extends Proxy {
    private int errorCounter = 0;


    public ProxyWrap(Proxy.Type var1, SocketAddress var2){
        super(var1, var2);
    }

    public int getErrorCounter() {
        return errorCounter;
    }

    public void incrementErrorCounter() {
        errorCounter++;
    }

    public void flushErrorCounter(){
        errorCounter = 0;
    }
}
