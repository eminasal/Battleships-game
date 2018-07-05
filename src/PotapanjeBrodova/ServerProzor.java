package PotapanjeBrodova;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Random;
import java.util.Vector;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;


import PotapanjeBrodova.Brod;



public class ServerProzor extends JFrame{
	
	/**Verziju klase, bitno ako je budemo kasnije mijenjali*/
    private static final long serialVersionUID  = 1;
    
	/** dimenzije display-a na kojem je pokrenut*/
    private final int sirina = java.awt.Toolkit.getDefaultToolkit().getScreenSize().width;
    private final int visina = java.awt.Toolkit.getDefaultToolkit().getScreenSize().height;
    
    /** dimenzije za aplikaciju, uzima se 1/2*/
    private final int appSirina = sirina * 1/2-50;
    private final int appVisina = visina * 1/2;
    
    /**postavljanje grafickih tabli za igrace */
    private JPanel tablaIgrac, tablaProtivnik;
    
    /**postavljanje vrijednosti za table igraca*/
    private JButton[] matricaIgrac, matricaProtivnik;
    

    /** Button za pocetak igre i postavljanje brodova */
    private JButton pocetak, formacija;
    
    /**Lista brodova*/
    private List<Brod> brodovi;
    
    /**Provjera slobodnih mjesta */
    private boolean [] zauzet;
    
    /**brojac brodova*/
    private int brojacIgrac, brojacProtivnik;
    
    /**Ko je na potezu*/
    private JLabel potez;
    private ActionListener potezAkcija;
    public boolean igrac1, igrac2, mojPotez;
    
    /**Konekcija */
    private ServerSocket serverSocket;
    private Server server;
    private String potezReakcija;
    
    
    
    /**Postavljanje prozora servera */
    public ServerProzor() {									//roza
    	getContentPane().setBackground(new java.awt.Color(255, 217, 204));
        inicijalizacija();
    }
    
    /**Pretvara stringove pozicija u integere */
    public int pretvoriPolje(String s) {
    	if(s.length()==1)
    		return (Integer.parseInt(s));
    	else if(s.length()==2)
    		return (((Integer.parseInt(s.substring(0,1))))*10+
    				((Integer.parseInt(s.substring(1,2)))));
    	else 
    		return -1;
    }
    
    
    public int napadProtivnika(int loc) {
        for (int i = 0; i < brodovi.size(); i++) {
            if (brodovi.get(i).pogodak(loc)) {
                if (brodovi.get(i).unisten()) {
                    return 2;
                } else {
                    return 1;
                }
            }
        }
        return 0;
    }
    
    
    public void inicijalizacija() {
    	
    	
        setTitle("Potapanje brodova - I igrac");
        /**mijenjanje velicine prozora  */
        setResizable(false);
        setSize(appSirina, appVisina);
        setLocationRelativeTo(null); //centriranje
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(null); //nekoristenje layout-a
        
        /**postavljanje ploce za igraca */
        tablaIgrac = new JPanel(new GridLayout(10, 10));
        matricaIgrac = new JButton[100];
        for (int i = 0; i < matricaIgrac.length; i++) {
        	matricaIgrac[i] = new JButton();
        	matricaIgrac[i].setBackground(Color.white);
            tablaIgrac.add(matricaIgrac[i]);
        }
        tablaIgrac.setBounds((int) (appSirina * 0.05), (int) (appVisina * 0.1), appVisina * 6/10, appVisina * 6/10);
        tablaIgrac.setVisible(true);
        
        /**postavljanje ploce za protivnika*/ 
        tablaProtivnik = new JPanel(new GridLayout(10, 10));
        matricaProtivnik = new JButton[100];
        for (int i = 0; i < matricaProtivnik.length; i++) {
            matricaProtivnik[i] = new JButton();
            matricaProtivnik[i].setBackground(Color.lightGray);
            matricaProtivnik[i].setActionCommand(String.valueOf((int) (i)) + "" );
            tablaProtivnik.add(matricaProtivnik[i]);
        }
        /** postavljanje dimenzija tabele za gadjanje */
        tablaProtivnik.setBounds((int) (appSirina * 0.55), (int) (appVisina * 0.1), appVisina * 6 / 10, appVisina * 6 / 10);
        tablaProtivnik.setVisible(true);
        
        /** pozicija za  ispisivanje aktivnosti*/
        potez = new JLabel();
        potez.setBounds((int) (appSirina * 0.40), (int) (appVisina * 0.01), 150, 20);
        
        /** pozicija za button za pocetak igre*/
        pocetak = new JButton("Zapocni igru");
        pocetak.setBounds((int) (appSirina* 0.36), (int) (appVisina * 0.8), 150, 40);
        pocetak.setBackground(Color.LIGHT_GRAY);
        
        /**Listener za button pocetak */
        pocetak.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                pocetak.setText("Prvi igrac je spreman.");
                pocetak.setEnabled(false);
                formacija.setEnabled(false);
                potez.setText("Cekanje drugog igraca.");
                igrac1 = true;
                konekcija();
            }
        });
        
        
        
        /** Button  za postavljanje brodova */
        formacija = new JButton("Postavi");
        formacija.setBounds((int) (appSirina * 0.05), (int) (appVisina * 0.75), 95, 30);
        formacija.setBackground(Color.LIGHT_GRAY);
        formacija.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                generisiBrodove();
            }
        });
        
        /** dodavanje tabli*/
        add(tablaIgrac);
        add(tablaProtivnik);
        /** dodavanje buttona */
        add(pocetak);
        add(formacija);
        /**dodavanje statusa*/
        add(potez);
        
        /** Iscrtavanje tabele*/
        /** setbounds(x,y,sirina, visina) x,y koordinatni sistem, visina sirini objekta*/
        for (int i = 0; i < 10; i++) {
            JLabel j1 = new JLabel(i+1 + "");
            j1.setBounds((int) (appSirina * 0.06) + i * appVisina * 6 / 100, (int) (appVisina  * 0.06), 15, 10);
            JLabel j2 = new JLabel(i+1 + "");
            j2.setBounds((int) (appSirina * 0.56) + i * appVisina  * 6 / 100, (int) (appVisina  * 0.06), 15, 10);
            JLabel j3 = new JLabel(i + 1 + "");
            j3.setHorizontalAlignment(SwingConstants.RIGHT);
            j3.setBounds((int) (appSirina * 0.02), (int) (appVisina  * 0.12) + i * appVisina  * 6 / 100, 15, 10);
            JLabel j4 = new JLabel(i + 1 + "");
            j4.setHorizontalAlignment(SwingConstants.RIGHT);
            j4.setBounds((int) (appSirina * 0.52), (int) (appVisina  * 0.12) + i * appVisina  * 6 / 100, 15, 10);
            add(j1);
            add(j2);
            add(j3);
            add(j4);
        }
        generisiBrodove();
        
        
        /** Listener za napad */
        potezAkcija = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                if (!igrac1 || !igrac2) {
                    return;
                }
                if (!mojPotez) {
                    return;
                }
                potezReakcija = event.getActionCommand(); // vraca string sa komandom 
                JButton button = (JButton) event.getSource(); //vraca refenrecu na objekat odakle je event dosao
                button.removeActionListener(potezAkcija); //nakon poteza ne mozemo vise pogadjati
                mojPotez = false; //prebacuje se na drugog igraca
            }
        };
            
    }
        
    
    private void konekcija() {
        try {
            if (serverSocket == null || serverSocket.isClosed()) {
                serverSocket = new ServerSocket(10000);
            }
            System.out.println("Listening");    
            server = new Server(serverSocket);
            server.start();
            System.out.println("Finished listening for Connection");    
        } 
        catch (IOException ioe) {
            System.out.println(ioe.toString());
        }
    }
    
    
    /** 		BRODOVI 		*/
    
    /** Generise brodove za server i resetuje brojace*/
    public void generisiBrodove() {
    	
        for (JButton i : matricaProtivnik) {
            i.removeActionListener(potezAkcija);
            i.addActionListener(potezAkcija);
            i.setIcon(null);
        }
        /** Boja table igraca */
        for (JButton i : matricaIgrac) {
            i.setIcon(null);
            i.setBackground(new Color(245, 245, 239));
        }
        /** izbor prvenstva igraca */
        igrac1 = igrac2 =false;
        mojPotez = true;

        zauzet = new boolean [matricaProtivnik.length];
        for (int i = 0; i < zauzet.length; i++) {
            zauzet[i] = false;
        }
        
        brodovi = new Vector<>();
        generisi5();
        generisi4();
        generisi3();
        generisi2();
        generisi1();
        
        //ispis brodova
        System.out.println("Igrac #1 je postavio brodove:");
        for (int i = 0; i < brodovi.size(); i++) {
            System.out.println(brodovi.get(i));
        }
        brojacIgrac=brojacProtivnik= brodovi.size();
    }
    
    /**brod velicine 5*/
    public void generisi5() { 	
    	Brod brod = new Brod();
        Random r  = new Random();
        int place = r.nextInt(100); 
        while(true) { //horizontalno
    	   if(place % 10 < 6)
    	   {
    		   if(zauzet[place]==false && zauzet[place+1]==false && zauzet[place+2]==false && zauzet[place+3]==false && zauzet[place+4]==false)
    		   {
    			   System.out.println("Pozicija broda 5 H :" + place);	
    			   for(int i=place; i<place+5; i++)
    			   {
    				   zauzet[i]=true;
    				   matricaIgrac[i].setBackground(Color.green);
    				   brod.dodajLokaciju(i);
    			   }
    	           brodovi.add(brod); 
    	           break;
    		   }    
    	   }
    	   else if(place / 10 < 6 ){ //vertikalno
    		   if(zauzet[place]==false && zauzet[place+10]==false && zauzet[place+20]==false && zauzet[place+30]==false && zauzet[place+40]==false)
    		   {
    			   System.out.println("Pozicija broda 5 V :" + place);	
    			   for(int i=place; i<place+50; i+=10)
    			   {
    				   zauzet[i]=true;
    				   matricaIgrac[i].setBackground(Color.green);
    				   brod.dodajLokaciju(i);
    			   }
    	           brodovi.add(brod); 
    	           break;
    		   }   
    	   }
    	   else
    	   r = new Random();
           place = r.nextInt(100);
       }
    }

    /**Brod velicine 4*/
    public void generisi4() {

    	Brod brod = new Brod();
        Random r  = new Random();
        int place = r.nextInt(100);
        
         while(true) { //horizontalno
    	   if(place % 10 < 7)
    	   {
    		   if(zauzet[place]==false && zauzet[place+1]==false && zauzet[place+2]==false && zauzet[place+3]==false )
    		   {
    			   System.out.println("Pozicija broda 4 H :" + place);	
    			   for(int i=place; i<place+4; i++)
    			   {
    				   zauzet[i]=true;
    				   matricaIgrac[i].setBackground(Color.blue);
    				   brod.dodajLokaciju(i);
    			   }
    	           brodovi.add(brod); 
    	           break;
    		   }    
    	   }
    	   else if(place / 10 < 7 ){ //vertikalno
    		   if(zauzet[place]==false && zauzet[place+10]==false && zauzet[place+20]==false && zauzet[place+30]==false )
    		   {
    			   System.out.println("Pozicija broda 4 V :" + place);	
    			   for(int i=place; i<place+40; i+=10)
    			   {
    				   zauzet[i]=true;
    				   matricaIgrac[i].setBackground(Color.blue);
    				   brod.dodajLokaciju(i);
    			   }
    	           brodovi.add(brod); 
    	           break;
    		   }   

    	   }
    	   else
    	   r = new Random();
           place = r.nextInt(100);
       }
    }

    /**Brod velicine 3*/
    public void generisi3() {

    	Brod brod = new Brod();
        Random r  = new Random();
        int place = r.nextInt(100);
        
         while(true) { //horizontalno
    	   if(place % 10 < 8)
    	   {
    		   if(zauzet[place]==false && zauzet[place+1]==false && zauzet[place+2]==false )
    		   {
    			   System.out.println("Pozicija broda 3 H :" + place);	
    			   for(int i=place; i<place+3; i++)
    			   {
    				   zauzet[i]=true;
    				   matricaIgrac[i].setBackground(Color.pink);
    				   brod.dodajLokaciju(i);
    			   }
    	           brodovi.add(brod); 
    	           break;
    		   }    
    	   }
    	   else if(place / 10 < 8 ){ //vertikalno
    		   if(zauzet[place]==false && zauzet[place+10]==false && zauzet[place+20]==false)
    		   {
    			   System.out.println("Pozicija broda 3 V :" + place);	
    			   for(int i=place; i<place+30; i+=10)
    			   {
    				   zauzet[i]=true;
    				   matricaIgrac[i].setBackground(Color.pink);
    				   brod.dodajLokaciju(i);
    			   }
    	           brodovi.add(brod); 
    	           break;
    		   }   

    	   }
    	   else
    	   r = new Random();
           place = r.nextInt(100);
       }
    }

    /** Brod velicine 2 */
    public void generisi2() {

    	Brod brod = new Brod();
        Random r  = new Random();
        int place = r.nextInt(100);
        
         while(true) { //horizontalno
    	   if(place % 10 < 9)
    	   {
    		   if(zauzet[place]==false && zauzet[place+1]==false)
    		   {
    			   System.out.println("Pozicija broda 2 H :" + place);	
    			   for(int i=place; i<place+2; i++)
    			   {
    				   zauzet[i]=true;
    				   matricaIgrac[i].setBackground(Color.red);
    				   brod.dodajLokaciju(i);
    			   }
    	           brodovi.add(brod); 
    	           break;
    		   }    
    	   }
    	   else if(place / 10 < 9 ){ //vertikalno
    		   if(zauzet[place]==false && zauzet[place+10]==false)
    		   {
    			   System.out.println("Pozicija broda 2 V :" + place);	
    			   for(int i=place; i<place+20; i+=10)
    			   {
    				   zauzet[i]=true;
    				   matricaIgrac[i].setBackground(Color.red);
    				   brod.dodajLokaciju(i);
    			   }
    	           brodovi.add(brod); 
    	           break;
    		   }   

    	   }
    	   else
    	   r = new Random();
           place = r.nextInt(100);
       }
    	
    }

    
    /**Brod velicine 1*/ 
    public void generisi1() {
    	
            Brod brod = new Brod();
            Random r  = new Random();
            int place = r.nextInt(100);
            
             while(true) {
        	   if(place>9 && place <90 && place %10 !=0 && place %10 !=9 )
        	   {
        		   if(zauzet[place +1] != true && zauzet[place + 10] !=true && zauzet[place -10] != true && zauzet[place-1] != true)
        		   {
        			   System.out.println("Pozicija broda 1:" + place);	
        	           zauzet[place] = true;
        	           matricaIgrac[place].setBackground(Color.yellow);
        	           brod.dodajLokaciju(place);
        	           brodovi.add(brod); 
        	           break;
        		   }    
        	   }
        	   else{
        		   if(zauzet[place] != true )
        		   {
        			   System.out.println("Pozicija broda 1:" + place);
        	           zauzet[place] = true;
        	           matricaIgrac[place].setBackground(Color.yellow);
        	           brod.dodajLokaciju(place);
        	           brodovi.add(brod); 
        	           break;
        		   }    

        	   }
       
        	   r = new Random();
               place = r.nextInt(100);
 
           }
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    /**Server class*/
    class Server extends Thread 
    {

        private Socket socket;
        private ServerSocket sv;
        private DataInputStream in;
        private DataOutputStream out;

        public Server(Socket socket) {
            this.socket = socket;
            try {
                in = new DataInputStream(socket.getInputStream());
                out = new DataOutputStream(socket.getOutputStream());
            } catch (IOException ex) {
                System.err.println(ex.toString());
            }
        }

        public Server(ServerSocket sv) {
            this.sv = sv;
        }

        @Override
        public void run() {
            System.out.println("Listening for connection...");
            try {
                this.socket = sv.accept();
                System.out.println("Connected " + socket.toString());
                try {
                    in = new DataInputStream(socket.getInputStream());
                    out = new DataOutputStream(socket.getOutputStream());
                } catch (IOException ex) {
                    System.err.println(ex.toString());
                }
                generisiBrodove();
                out.writeUTF("READY");
                igrac1 = true;
                if (in.readUTF().equals("READY")) {
                    
                    potez.setText("CONNECTED");
                    igrac2 = true;
                }
                while (brojacIgrac != 0 && brojacProtivnik != 0) { //uslov za kraj igre
                    if (!mojPotez) // provjera ko je na potezu
                    {
                        potez.setText("Protivnik je na redu!");
                        potez.setForeground(Color.red);
                        
                        int loc = pretvoriPolje(in.readUTF());
                        int result = napadProtivnika(loc);
                        
                        if (result >= 1) {
                            matricaIgrac[loc].setIcon(new ImageIcon("X.png"));
                            if (result == 2) {
                                brojacIgrac--;
                                out.writeUTF("SINK");
                            } else {
                                out.writeUTF("HIT");
                            }
                        }
                        else if(result == 0) {
                            matricaIgrac[loc].setIcon(new ImageIcon("miss.png"));
                            out.writeUTF("MISS");
                        }

                        mojPotez = true;
                    } else { //drugi igrac
                        potez.setText("Sad je tvoj red!");
                        potez.setForeground(Color.green);
                        try {
                            while (mojPotez) {
                                continue;
                            }
                            out.writeUTF(potezReakcija);
                            switch(in.readUTF()) {
                            case "HIT":{
                           	 	matricaProtivnik[pretvoriPolje(potezReakcija)].setIcon(new ImageIcon("X.png"));    
                           	 	break;
                            }
                            case "SINK":{
                           	 	matricaProtivnik[pretvoriPolje(potezReakcija)].setIcon(new ImageIcon("sank.png"));  
                           	 	break;
                            }
                            case "MISS": {
                            	matricaProtivnik[pretvoriPolje(potezReakcija)].setIcon(new ImageIcon("miss.png"));  
                            	break;
                            }
                          } 
                        } 
                        catch (Exception ex) {
                            System.out.println(ex.toString());
                        }
                    }
                }

                if (brojacIgrac == 0){
                	JOptionPane.showMessageDialog(null, 
                			"Cestitamo #2 igrac, pobijedili ste!", 
                            "Kraj", JOptionPane.INFORMATION_MESSAGE);
                	System.exit(ABORT);
                }
                socket.close();
                sv.close();
                setVisible(false); 
                dispose();
               
            } 
            catch (IOException ex) {
                System.err.println(ex.toString());
            }

        }

    }

    public static void main(String[] args) {
        
    	Object [] options = {"Server", "Klijent"};
    	int n = JOptionPane.showOptionDialog(null, "Cao igraci, da li zeliti biti server ili klijent?",
    			"Start", JOptionPane.YES_NO_OPTION,
    			JOptionPane.QUESTION_MESSAGE,
    			null, options, options[0]); 
    	if(n==JOptionPane.YES_OPTION)
    		new ServerProzor().setVisible(true);
    	else if (n==JOptionPane.NO_OPTION)
    		new ClientProzor().setVisible(true);
    	
    	      
    }

}