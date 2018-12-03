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
	public void start(String dir, int porta) {
		this.dir=dir;
		this.porta=porta;
		this.cwnd=1;
		this.ssthresh=10000;
		this.id=1;
		this.janela= new Vector<Pacote>();
		this.clientes=new Hashtable<Integer, Conec>();
		System.out.println("Iniciando......");
		try {
			this.servidor= new DatagramSocket(porta);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			System.out.println("Erro ao iniciar servidor");
			e.printStackTrace();
		}
		Runnable r = () -> {
			this.recebe();
		};
		Thread t = new Thread(r);
		t.start();
		Runnable run = () -> {
			this.envia();
		};
		Thread th = new Thread(run);
		th.start();
	}
	private void recebe() {
		while(this.isFim==false) {
			System.out.println("recebendo.....");
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
			System.out.println("Servidor recebeu "+p.getConnectionID()+" "+ p.getS());
			if(p.getConnectionID()==0 && p.getS()==true) {
				Pacote aux=this.pacoteParaEnviar(p,1,this.id);
				aux.SetS(true);
				aux.SetA(true);
				System.out.println("recebe");
				this.novoCliente(recebPacote.getAddress(),recebPacote.getPort() ,p,aux.getConnectionID());
				this.janela.add(aux);
				this.id++;
				System.out.println("Adicionou");
			}else if(p.getConnectionID()!=0 && p.GetF()==false) {
				int id=p.getConnectionID();
				if(clientes.containsKey(id)) {
					Conec c=clientes.get(id);
					if(c.isClose()==false) {
						if(c.getUltimoComf().equals(p)==false) {
							System.out.println();
							System.out.println("Escrevendo..");
							c.escreverAq(new String(p.getDados()));
							c.setUltimoComf(p);
							Pacote aux=this.pacoteParaEnviar(p, 512,p.getConnectionID());
							aux.SetS(true);
							System.out.println("recebe "+p.getConnectionID());
							janela.add(aux);
						}else {
							c.setUltimoComf(p);
							System.out.println("recebe");
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
							Pacote enviar=this.pacoteParaEnviar(p, 1,p.getConnectionID());
							enviar.SetF(true);
							System.out.println("recebe");
							janela.add(enviar);
							Pacote aux=this.pacoteParaEnviar(p, 1,p.getConnectionID());
							aux.setAckNumber(0);
							janela.add(enviar);
						}else {
							c.setUltimoComf(p);
							System.out.println("recebe");
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


	private Pacote pacoteParaEnviar(Pacote p,int ack,int id) {
		Pacote enviar= new Pacote();
		enviar.setConnectionID((short)id);
		enviar.setAckNumber(p.getSequenceNumber()+ack);
		enviar.SetA(true);
		enviar.setSequenceNumber(4321);
		return enviar;
	}
	private void novoCliente(InetAddress endreco,int porta, Pacote p,int id) {
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
		System.out.println("Cliente novo "+id);
		Integer i=new Integer(id);
		clientes.put(i, con);
		System.out.println("Cliente novo com id "+clientes.get(i));
	}

	private void envia() {
		while(this.isFim==false) {
			int aux=0;
			if(this.cwnd<=this.janela.size()) {
				aux=cwnd;
			}else {
				aux=this.janela.size();
			}
			for(int i=0;i<aux;i++) {
				byte[] enviar=null;
				Pacote p=this.janela.remove(i);
				if(p!=null) {
					
					enviar=p.getPacote();
					Integer inteiro=new Integer(p.getConnectionID());
					System.out.println("A enviar " +p.getConnectionID()+" "+clientes.get(inteiro)+ " "+p.getA());
							if(clientes.containsKey(inteiro)) {
							Conec c=clientes.get((int)p.getConnectionID());
							System.out.println("Enviar..."+c.getId());
							DatagramPacket enviarP = new DatagramPacket(enviar,
									enviar.length, c.getEndereco(), c.getPorta());
							try {
								System.out.println("enviando..."+p.getConnectionID());
								this.servidor.send(enviarP);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								System.out.println("Erro ao enviar "+p.getConnectionID());
								e.printStackTrace();
							}
							}
						
				}
				
				
				
			}
			if(this.cwnd<this.ssthresh) {
				this.cwnd=this.cwnd*2;
			}else {
				this.cwnd++;
			}
		}
	}
}
