package tcp;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Hashtable;
import java.util.Vector;

import pacote.Pacote;

public class Servidor {
	private int id;
	private String dir;
	private boolean isFim;
	private int porta; 
	private int sequenceNumber;
	private DatagramSocket servidor;
	private Vector<Pacote> janela;
	private Hashtable<Integer, Conec> clientes;
	public void start() {
		this.id=0;
		this.janela= new Vector<Pacote>();
		this.clientes=new Hashtable<Integer, Conec>();
		try {
			this.servidor= new DatagramSocket(porta);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			System.out.println("Erro ao iniciar servidor");
			e.printStackTrace();
		}
	}
	private void recebe() {
		byte[] recebeDados = new byte[524];
		DatagramPacket recebPacote = new DatagramPacket(recebeDados,recebeDados.length);
		try {
			servidor.receive(recebPacote);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Erro ao receber pacote");
			e.printStackTrace();
		}
		Pacote p= new Pacote();
		p.setPacote(recebeDados);
		if(p.getConnectionID()==0 && p.getS()==true) {
			Pacote enviar= new Pacote();
			enviar.setConnectionID((short) this.id);
			enviar.SetS(true);
			enviar.setAckNumber(p.getAckNumber()+1);
			enviar.SetA(true);
			janela.add(enviar);
			File f = new File(dir+""+id+".file"); 
			try {
				f.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("Erro ao criar arquivo");
				e.printStackTrace();
			}
			Conec con= new Conec();
			con.setId(id);
			con.setEndereco(recebPacote.getAddress());
			con.setPorta(recebPacote.getPort());
			con.setAq(f);
			con.setUltimoComf(p);
			//---
			con.tempo();
			
			clientes.put(id, con);
			
			
		}
	}
	
	private void envia() {
		
	}
}
