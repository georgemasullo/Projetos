package tcp;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Hashtable;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import pacote.Pacote;

public class Servidor {
	private int id;
	private String dir;
	private boolean isFim;
	private boolean recebeuP;
	private int porta; 
	private int cwnd;
	private int ssthresh;
	private DatagramSocket servidor;
	private Vector<Pacote> janela;
	private Hashtable<Integer, Conec> clientes;
	public void start() {
		this.cwnd=1;
		this.ssthresh=10000;
		this.id=1;
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
		while(this.isFim==false) {
			this.controleConges();
			byte[] recebeDados = new byte[524];
			DatagramPacket recebPacote = new DatagramPacket(recebeDados,recebeDados.length);
			try {
				servidor.receive(recebPacote);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("Erro ao receber pacote");
				e.printStackTrace();
			}
			this.recebeuP=true;
			Pacote p= new Pacote();
			p.setPacote(recebeDados);
			if(p.getConnectionID()==0 && p.getS()==true) {
				this.novoCliente(recebPacote.getAddress(),recebPacote.getPort() ,p);
				Pacote aux=this.pacoteParaEnviar(p,1);
				this.janela.add(aux);
				this.id++;
			}else if(p.getConnectionID()!=0 && p.GetF()==false) {
				int id=p.getConnectionID();
				if(clientes.containsKey(id)) {
					Conec c=clientes.get(id);
					if(c.isClose()==false) {
						if(c.getUltimoComf().equals(p)==false) {
							c.escreverAq(new String(p.getPacote()));
							c.setUltimoComf(p);
							Pacote aux=this.pacoteParaEnviar(p, 512);
							aux.SetS(true);
							janela.add(aux);
						}else {
							c.setUltimoComf(p);
							janela.add(p);
						}
					}
				}
			}else if(p.getConnectionID()!=0 && p.GetF()==true) {
				int id=p.getConnectionID();
				if(clientes.containsKey(id)) {
					Conec c=clientes.get(id);
					if(c.isClose()==false) {
						if(c.getUltimoComf().equals(p)==false) {
							c.setUltimoComf(p);
							Pacote enviar=this.pacoteParaEnviar(p, 1);
							enviar.SetF(true);
							janela.add(enviar);
							Pacote aux=this.pacoteParaEnviar(p, 1);
							aux.setAckNumber(0);
							janela.add(enviar);
						}else {
							c.setUltimoComf(p);
							janela.add(p);
						}
					}
				}
			}else if(p.getConnectionID()!=0 && p.getA()==true) {
				int id=p.getConnectionID();
				if(clientes.containsKey(id)) {
					Conec c=clientes.get(id);
					if(c.getUltimoComf().equals(p)==false) {
						if(c.getUltimoComf().getAckNumber()+1 == p.getAckNumber() && 
								c.getUltimoComf().GetF()==true) {

							c.setUltimoComf(p);
							c.close();

						}
					}
				}
			}else {
				continue;
			}
		}
	}

	private void controleConges() {
		this.recebeuP=false;
		Runnable r = () -> {
			ExecutorService es = Executors.newSingleThreadExecutor();
			Callable<Void> c = () -> {
				while(this.recebeuP==false) {
					continue;
				}
				return null;
			};
			Future<Void> f = es.submit(c);
			try {
				f.get(500, TimeUnit.MILLISECONDS);

			}catch (InterruptedException | ExecutionException | TimeoutException e) {
				this.ssthresh=this.cwnd;
				this.cwnd=1;
			}
		};
		Thread t = new Thread(r);
		t.start();
	}


	private Pacote pacoteParaEnviar(Pacote p,int ack) {
		Pacote enviar= new Pacote();
		enviar.setConnectionID((short) this.id);
		enviar.setAckNumber(p.getAckNumber()+ack);
		enviar.SetA(true);
		enviar.setSequenceNumber(4321);
		return enviar;
	}
	private void novoCliente(InetAddress endreco,int porta, Pacote p) {
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
		con.setEndereco(endreco);
		con.setPorta(porta);
		con.setAq(f);
		con.setUltimoComf(p);
		con.tempo();

		clientes.put(id, con);
	}

	private void envia() {
		while(this.isFim==false) {
			int aux=0;
			if(this.cwnd<this.janela.size()) {
				aux=cwnd;
			}else {
				aux=this.janela.size();
			}
			for(int i=0;i<aux;i++) {
				byte[] enviar;
				Pacote p=this.janela.remove(i);
				enviar=p.getPacote();
				Conec c=clientes.get((int)p.getConnectionID());
				DatagramPacket enviarP = new DatagramPacket(enviar,
						enviar.length, c.getEndereco(), c.getId());
				try {
					this.servidor.send(enviarP);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					System.out.println("Erro ao enviar "+p.getConnectionID());
					e.printStackTrace();
				}
			}
			if(this.cwnd<this.ssthresh) {
				this.cwnd=this.cwnd*2;
			}
		}
	}
}
