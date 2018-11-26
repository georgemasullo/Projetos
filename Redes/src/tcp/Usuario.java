package tcp;

import java.net.DatagramSocket;

public class Usuario {
	
	private String endereco;
	private int porta;
	private int id;
	private DatagramSocket clienteSocket;
	private boolean isHandshake;
	
	
	public Usuario(String endereco, int porta) {
		isHandshake=true;
	}
	
	private void envia(byte dados[]) {
		//implementacao do enviar
		if(isHandshake) {
			//faz Handshake 
		}
	}

}
