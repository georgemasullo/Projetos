package tcp;

import pacote.Pacote;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


public class Usuario {
	
	private int tma;
	private int porta;
	private short id;
	private DatagramSocket clienteSocket;
	private boolean isHandshake;
	private int sequenceNumber = 12345;
	private int marcadorArq = 0;
	private File arq;
	private ArrayList<Pack> janela ;
	private int cwnd=1;
	private int sstresh=10000;
	private boolean recebeuP;
	private boolean ackF;
	
	
	public Usuario(int serv, int porta,String aqr) {
		isHandshake=true;
		this.porta = serv;
		arq = new File(aqr);
		janela  = new ArrayList<Pack>();
		try {
			clienteSocket= new DatagramSocket(porta);
		} catch (SocketException e) {
			System.out.println("Erro ao iniciar o servidor");
			e.printStackTrace();
		}
	}
	
	
	private void enviar(Pacote pac) {
		if(isHandshake) {
			//faz Handshake int sequencia, int ack,short connecid,int asf,byte[] dados
			Pacote hand = new Pacote(this.sequenceNumber,0,(short)0,2);
			hand.SetS(true);
			this.sequenceNumber++;
			byte pack[] = hand.getPacote();
			InetSocketAddress i =new InetSocketAddress("127.0.0.1", this.porta);
			DatagramPacket handShake = new DatagramPacket(pack,pack.length,i.getAddress(),this.porta);
			try {
				System.out.println("Enviou S");
				clienteSocket.send(handShake);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("Erro ao enviar pacote\n");
				e.printStackTrace();
			}
			try {
				System.out.println("Recebou S");
				clienteSocket.receive(handShake);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("Erro ao receber pacote\n");
				e.printStackTrace();
			}
			hand.setPacote(pack);
			System.out.println("Recebeu "+hand.getConnectionID()+" "+hand.getA()+" "+hand.getS());
			if(hand.getA()==true && hand.getS()==true) {
				this.id = hand.getConnectionID();
				pac.setConnectionID(this.id);
				pack = pac.getPacote();
				this.sequenceNumber+=512;
				handShake = new DatagramPacket(pack,pack.length,i.getAddress(),this.porta);
				try {
					System.out.println("Envia.."+pac.getConnectionID()+" "+pac.getAckNumber()
					+" "+pac.getA()+" "+pac.GetF());
					clienteSocket.send(handShake);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					System.out.println("Erro ao enviar pacote\n");
					e.printStackTrace();
				}
				System.out.println("Enviou..");
			this.sequenceNumber++;
			this.isHandshake = false;
		
		}
			
		}else {
			System.out.println("Enviando "+pac.getConnectionID());
			pac.setConnectionID(this.id);
			pac.setSequenceNumber(this.sequenceNumber);
			this.sequenceNumber+=512;
			byte[] n = pac.getPacote();
			DatagramPacket enviar = new DatagramPacket(n,n.length);
			try {
				clienteSocket.send(enviar);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("Erro ao enviar pacote\n");
				e.printStackTrace();
			}
		}
	}
	
	public void tempo() {
		this.ackF = false;
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
				f.get(2, TimeUnit.SECONDS);

			}catch (InterruptedException | ExecutionException | TimeoutException e) {
				janela.clear();
				clienteSocket.close();
				System.exit(0);
			}
		};
		Thread t = new Thread(r);
		t.start();
		
	}
	
	public void recebeu() {
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
				f.get(10, TimeUnit.SECONDS);

			}catch (InterruptedException | ExecutionException | TimeoutException e) {
				janela.clear();
				clienteSocket.close();
				System.out.println("Erro servidor nao esta retornando");
				System.exit(0);
			}
		};
		Thread t = new Thread(r);
		t.start();
	}
	
	public void recebe() {
		byte[] recebeDados = new byte[12];
		DatagramPacket recebPacote = new DatagramPacket(recebeDados,recebeDados.length);
		try {
			clienteSocket.receive(recebPacote);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Pacote pec = new Pacote();
		pec.setPacote(recebeDados);
		System.out.println(pec.getAckNumber()+"     "+pec.GetF()+"  "+pec.getA());
		for(int i=0;i<janela .size();i++) {
			System.out.println("Arquivo na janela"+janela.get(i).getPack().getAckNumber()+janela.get(i).isAck());
			System.out.println(pec.getAckNumber()-512+" s "+janela .get(i).getPack().getSequenceNumber() );
			if((pec.getAckNumber()-janela.get(i).getPack().getDados().length) == janela .get(i).getPack().getSequenceNumber() && pec.getA()) {
				janela .get(i).setAck(true);
				if(janela.get(i).getPack().GetF()) {
					this.tempo();
				}
			}
		}
		if(janela .get(0).isAck()) {
			for(int i=0;i<janela .size();i++) {
				if(janela .get(i).isAck()) {
					janela .remove(i);
				}
			}
		}
		if(pec.GetF()) {
			Pacote x = new Pacote(this.sequenceNumber,pec.getSequenceNumber()+1,this.id,2);
			Pack n = new Pack();
			n.setPack(x);
			n.setEnviar(true);
			janela.add(n);
		}
		this.recebeuP = true;
		this.controlConges();
		this.recebe();
		
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
			String linha;
			while((linha = br.readLine()) != null ) {
				System.out.println("Lendo arq");
				dados += linha;
			}
			return dados.getBytes();
		}else {
			System.out.println("Arquivo Vazio\n");
			System.exit(0);
			return null;
		}
	}

	public Pacote pacotePraEnviar(Pacote p,byte[] dados) {
		p.setConnectionID(this.id);
		p.setSequenceNumber(this.sequenceNumber);
		p.SetA(false);
		p.SetF(false);
		p.SetS(false);
		p.setDados(dados);
		p.setAckNumber(0);
		return p;
	}
	
	public void start() {
		byte[] arquivo = null;
		try {
			System.out.println("Iniciando...");
			arquivo = this.arquivoByte(arq);
			tma=arquivo.length;
			boolean p = true;
			System.out.println("Iniciando...");
			while(p) {
				int aux;
				byte []dados;
				System.out.println(arquivo.length);
				Runnable run = () ->{
					while(true) {
						enviarFyn(tma);
					}
				};
				Thread t = new Thread(run);
				t.start();
				
				if((this.marcadorArq+512)<arquivo.length) {
					aux=marcadorArq+512;
					dados = new byte[512];
				}else {
					aux=arquivo.length;
					dados = new byte[aux-this.marcadorArq];
					p=false;
				}
			
				for(int i=0;this.marcadorArq < aux && i<512;this.marcadorArq++,i++) {
					dados[i] = arquivo[this.marcadorArq];
				}
				Pacote pack = new Pacote();
				this.pacotePraEnviar(pack, dados);
				Pack pec = new Pack();
				pec.setPack(pack);
				pec.setEnviar(true);
				janela.add(pec);
			}
			System.out.println("Iniciando...");
			Runnable run = () ->{
				while(true) {
					int au;
					if(this.cwnd<=this.janela.size()) {
						au=cwnd;
					}else {
						au=this.janela.size();
					}
					for(int i=0;i<au;i++) {
						if(this.janela.get(i).isEnviar()) {
							System.out.println("enviando...");
							this.enviar(this.janela.get(i).getPack());
							this.janela.get(i).setEnviar(false);
						}
					}
				}
			};
			Thread t = new Thread(run);
			t.start();
			System.out.println("Iniciando...");
			
			Runnable r = () ->{
				while(true)
					this.recebe();
			};
			Thread k = new Thread(r);
			k.start();
			
			if(this.cwnd<this.sstresh) {
				this.cwnd*=2;
			}else {
				this.cwnd++;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}


	private void enviarFyn(int tam) {
		while(true) {
			//System.out.println("Janela "+ janela.size());
		if(marcadorArq>=tam) {
			if(janela.isEmpty()) {
				System.out.println();
				Pacote k = new Pacote(this.sequenceNumber,0,this.id,3);
				Pack j = new Pack();
				j.setPack(k);
				j.setEnviar(true);
				janela.add(j);
			}
		}
		}
	}
	
	public void controlConges() {
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
				this.sstresh=this.cwnd;
				this.cwnd=1;
			}
		};
		Thread t = new Thread(r);
		t.start();
	}
	
}
