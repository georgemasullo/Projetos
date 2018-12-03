package tcp;

import pacote.Pacote;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class Usuario {
	
	private String endereco;
	private int porta;
	private short id;
	private DatagramSocket clienteSocket;
	private boolean isHandshake;
	private int sequenceNumber = 12345;
	private int marcadorArq = 0;
	private File arq;
	
	
	public Usuario(String endereco, int porta) {
		isHandshake=true;
		this.endereco = endereco;
		this.porta = porta;
		arq = new File("C:\\Users\\Paulo Alencar\\Documents\\redes.txt");
	}
	
	private void enviar(byte dados[]) {
		if(isHandshake) {
			//faz Handshake int sequencia, int ack,short connecid,int asf,byte[] dados
			Pacote hand = new Pacote(this.sequenceNumber,0,(short)0,2,dados);
			this.sequenceNumber++;
			byte pack[] = hand.getPacote();
			DatagramPacket handShake = new DatagramPacket(pack,pack.length);
			try {
				clienteSocket.send(handShake);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				clienteSocket.receive(handShake);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			hand.setPacote(pack);
			if(hand.getA()==true && hand.getS()==true) {
				this.id = hand.getConnectionID();
				hand.SetA(true);
				hand.setConnectionID(this.id);
				hand.setDados(dados);
				hand.SetF(false);
				hand.SetS(false);
				hand.setSequenceNumber(sequenceNumber);
				pack = hand.getPacote();
				handShake = new DatagramPacket(pack,pack.length);
				try {
					clienteSocket.send(handShake);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}	
			}else {
				Pacote c = new Pacote(this.sequenceNumber,0,this.id,0,dados);
				byte[] n = c.getPacote();
				DatagramPacket enviar = new DatagramPacket(n,n.length);
				try {
					clienteSocket.send(enviar);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		
		}
	}
	
	public void recebe() {
		
	}

	
	public byte[] arquivoByte(File arq) throws IOException {
		if(arq.canRead()) {
			FileReader fr = null;
			try {
				fr = new FileReader(arq);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			BufferedReader br = new BufferedReader(fr);
			String dados = null;
			while(br!=null) {
				dados += br.readLine();
			}
			return dados.getBytes();
		}else {
			System.out.println("Arquivo Vazio\n");
			return null;
		}
	}
	
}
