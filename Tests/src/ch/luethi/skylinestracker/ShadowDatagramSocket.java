package ch.luethi.skylinestracker;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

import java.io.IOException;
import java.net.*;

/**
 * Created by andreas on 06.09.15.
 */
@Implements(DatagramSocket.class)

public class ShadowDatagramSocket extends DatagramSocket {

    @RealObject
    private DatagramSocket datagramSocket;

    private DatagramPacket pack;

    protected ShadowDatagramSocket(DatagramSocketImpl socketImpl) {
        super(socketImpl);
    }

    public void __constructor__() throws SocketException {
    }

    public void __constructor__(int aPort) throws SocketException {
    }

    public void __constructor__(int aPort, InetAddress addr) throws SocketException {
    }

    public void __constructor__(SocketAddress localAddr) throws SocketException {
    }

    @Implementation
    public void send(DatagramPacket pack) throws IOException {
        this.pack = pack;
    }


    public DatagramPacket getDatagramPacket() {
        return pack;
    }
}
