package tcp;

import pacote.Pacote;
import java.net.DatagramSocket;

public class Usuario {
	
	private String endereco;
	private int porta;
	private int id;
	private DatagramSocket clientSocket;
	private int sequenceNumber = 12345;// numero de sequencia inicial
	
	public Usuario(String endereco, int porta) {
		this.endereco = endereco;
		this.porta = porta;
		
	}

	public void enviar(byte[] dados) {
		Pacote pack = new Pacote(this.sequenceNumber,0,(short)0,(short)2,dados);
		
		
	}
	
}
