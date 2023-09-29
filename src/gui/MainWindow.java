package gui;

import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListTagCommand;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.ResetCommand.ResetType;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;
import javax.swing.JTextArea;
import java.awt.Color;
import javax.swing.JTextField;
import javax.swing.JScrollPane;

public class MainWindow extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
    private JLabel lblVersionName;
    private JButton btnUpdate;
    private JTextArea txtMensaje;
    private boolean fileState=false;
    private JButton btnIniciar;
    private JTextField txtToken;
    private JLabel lblTokenTitle;
    private JScrollPane scrollPane;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainWindow frame = new MainWindow();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public MainWindow() {
		setResizable(false);
		setTitle("Bot Fefi");
		setIconImage(Toolkit.getDefaultToolkit().getImage(MainWindow.class.getResource("/img/fefi.png")));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 446, 328);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblVersionTitle = new JLabel("Version:");
		lblVersionTitle.setFont(new Font("Tahoma", Font.BOLD, 16));
		lblVersionTitle.setBounds(21, 21, 77, 20);
		contentPane.add(lblVersionTitle);
		
		lblVersionName = new JLabel("...");
		lblVersionName.setFont(new Font("Tahoma", Font.BOLD, 16));
		lblVersionName.setBounds(108, 21, 167, 20);
		contentPane.add(lblVersionName);
		
		Thread buttonThread = new Thread(new Runnable() {
            public void run() {
            	if(fileState==false) {
					cloneGitProject();
					
				}else if(fileState==true){
					updateGitProject();
				}               
            }
        });
		
		Thread nodeThread = new Thread(new Runnable() {
            public void run() {
            	startNode();          
            }
        });
		
		btnUpdate = new JButton("Actualizar");
		btnUpdate.setBackground(new Color(192, 192, 192));
		btnUpdate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnIniciar.setEnabled(true);
				waitMessage();
				buttonThread.start();
									
			}
		});
		btnUpdate.setBounds(300, 22, 105, 23);
		contentPane.add(btnUpdate);
		
		scrollPane = new JScrollPane();
		scrollPane.setBounds(21, 71, 384, 103);
		contentPane.add(scrollPane);
		
		txtMensaje = new JTextArea();
		txtMensaje.setFont(new Font("Tahoma", Font.PLAIN, 14));
		txtMensaje.setLineWrap(true);
		txtMensaje.setWrapStyleWord(true);
		scrollPane.setViewportView(txtMensaje);
		
		btnIniciar = new JButton("Iniciar bot");
		btnIniciar.setBackground(new Color(192, 192, 192));
		btnIniciar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				initiatedCorrectly();
				btnIniciar.setEnabled(false);
				nodeThread.start();
			}
		});
		btnIniciar.setEnabled(false);
		btnIniciar.setBounds(156, 242, 119, 29);
		contentPane.add(btnIniciar);
		
		txtToken = new JTextField();
		txtToken.setBounds(81, 199, 228, 20);
		contentPane.add(txtToken);
		txtToken.setColumns(10);
		
		lblTokenTitle = new JLabel("Token:");
		lblTokenTitle.setFont(new Font("Tahoma", Font.BOLD, 13));
		lblTokenTitle.setBounds(21, 201, 46, 14);
		contentPane.add(lblTokenTitle);
		
		JButton btnSave = new JButton("Guardar");
		btnSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				insertToken();
			}
		});
		btnSave.setBackground(new Color(192, 192, 192));
		btnSave.setBounds(316, 198, 89, 23);
		contentPane.add(btnSave);
		checkDirectory();
	}
	
	void startNode() {
		try {
            // Execute Node.js
            String comando = "node Bot_Fefi/index.js";

            // Execute command
            Process proceso = Runtime.getRuntime().exec(comando);

            // Read logs
            BufferedReader reader = new BufferedReader(new InputStreamReader(proceso.getInputStream()));
            String linea;
            while ((linea = reader.readLine()) != null) {
                txtMensaje.append(linea+"\n");
            }         

        } catch (IOException e) {
        	errorMessage(e);
        }
	}
	
	void insertToken() {
		String token = txtToken.getText().trim();
		String rutaArchivo = "./Bot_Fefi/fefiData/token.js";
        String nuevoContenido = "module.exports.token= '"+token+"';";
        
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(rutaArchivo))) {
            bw.write(nuevoContenido);
        } catch (IOException e) {
        	errorMessage(e);
        }
        txtToken.setText("");
        tokenSaved();
        btnIniciar.setEnabled(true);
	}
	
	void checkDirectory() {
		String directoryPath1 = "./Bot_Fefi";
		String directoryPath2 = "./Bot_Fefi/.git";
		File directoryBotFefi = new File(directoryPath1);
        File directoryGit = new File(directoryPath2);
        //Check directories
        if (directoryBotFefi.exists() && directoryBotFefi.isDirectory()) {
            if (directoryGit.exists() && directoryGit.isDirectory()) {
            	versionGit();
            	fileState = true;
            	btnIniciar.setEnabled(true);
            } else {
            	notFoundMessage();
            	fileState = false;
            	btnIniciar.setEnabled(false);
            }
        } else {
        	fileState = false;
        	notFoundMessage();
        	btnIniciar.setEnabled(false);
        }
		
	}
	void versionGit() {
		String localRepoPath = "./Bot_Fefi";
		try {
            Repository repository = new RepositoryBuilder().setGitDir(new File(localRepoPath + "/.git")).build();

            // Git repository
            Git git = new Git(repository);

            // Tags list
            ListTagCommand listTagCommand = git.tagList();
            List<Ref> tags = listTagCommand.call();

            if (!tags.isEmpty()) {
                // Last tag
                Ref latestTag = tags.get(tags.size() - 1);
                lblVersionName.setText(latestTag.getName().replace("refs/tags/", ""));
            } else {
            	lblVersionName.setText("Desconocida");
            }
            git.close();
            repository.close();
        } catch (Exception e) {
        	errorMessage(e);
        }
	}
	void updateGitProject() {
		String localRepoPath = "./Bot_Fefi";
        try {
            // Repository
            Git git = Git.open(new File(localRepoPath));

            //PullCommand Object
            PullCommand pullCommand = git.pull();
            pullCommand.call();
            //ResetCommand object
            ResetCommand resetCommand = git.reset().setMode(ResetType.HARD);
            resetCommand.call();           
            updatedMessage();
            versionGit();
            checkDirectory();
        } catch (Exception e) {
        	errorMessage(e);
        }
	}
	void cloneGitProject() {
		// Repository URL
        String repoUrl = "https://github.com/Goichi11/Bot_Fefi";
        // Directory Path
        String localPath = "./Bot_Fefi";
        try {
            // Clone command
            CloneCommand cloneCommand = Git.cloneRepository()
                .setURI(repoUrl)
                .setDirectory(new File(localPath));                   
            
            // Clone repository
            Git git = cloneCommand.call();
            updatedMessage();
            versionGit();
            git.close();
            fileState = true;
            checkDirectory();
        } catch (Exception e) {
        	errorMessage(e);
        }
	}
	void notFoundMessage() {
		lblVersionName.setText("No encontrada");
	}
	void updatedMessage() {
		Date hour = new Date();
		SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        String hourFormat = format.format(hour);

		txtMensaje.setText("["+hourFormat+"]"+" Actualizado correctamente"+"\n");
	}
	void initiatedCorrectly() {
		Date hour = new Date();
		SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        String hourFormat = format.format(hour);

		txtMensaje.setText("["+hourFormat+"]"+" El bot ha sido iniciado y se esta conectando"+"\n"+
							"["+hourFormat+"]"+" Recordar apagar el bot con f!apagar si se desea iniciar de nuevo para evitar Fefis duplicados xd"+"\n");
	}
	void tokenSaved() {
		Date hour = new Date();
		SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        String hourFormat = format.format(hour);

		txtMensaje.setText("["+hourFormat+"]"+" El token se ha guardado"+"\n");
	}
	private boolean waitMessage() {
		Date hour = new Date();
		SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        String hourFormat = format.format(hour);

		txtMensaje.setText("["+hourFormat+"]"+" Se esta descargando el bot. Espere un momento, tardara menos de un minuto..."+"\n");
		return true;
	}
	void errorMessage(Exception e) {     
		Date hour = new Date();
		SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        String hourFormat = format.format(hour);

		txtMensaje.setText("["+hourFormat+"]"+" Error:"+"\n"+e);
	}
}
