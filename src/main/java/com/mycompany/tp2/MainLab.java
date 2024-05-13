/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.mycompany.tp2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author barro
 */
public class MainLab extends javax.swing.JFrame {

    // Valores para la conexión a la base de datos (su nombre, URL, Usuario y Contraseña)
    private static final String DB_NAME = "Arqueología";
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/" + DB_NAME;
    private static final String DB_USER = "postgres";
    private static final String DB_PWD = "admin";
    
    // Mensajes de error
    private static final String ERROR_MSG_INSERT = "Error al intentar dar de alta a esta persona.";
    private static final String ERROR_MSG_INSERT_INPUT = "No se admiten campos vacíos.";
    
    // Objetos utilizados para interactuar con la base de datos
    // (conexión, realizar consultas con y sin parámetros, y recibir los resultados)
    private static Connection conn = null;
    private static Statement query = null;
    private static PreparedStatement p_query = null;
    private static ResultSet result = null;
    /**
     * Creates new form MainLab
     * @throws java.sql.SQLException
     */
    public MainLab() throws SQLException {
        initComponents();

    //CREACION DE LAS TABLAS PARA LA BASE DE DATOS 
            
        conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PWD);
        query = conn.createStatement();
        query.execute("CREATE TABLE IF NOT EXISTS Sitios("
                + "S_Cod VARCHAR(50) NOT NULL, "
                + "S_Localidad VARCHAR(50) NOT NULL, "
                + "PRIMARY KEY (S_Cod))");
        
        query.execute("CREATE TABLE IF NOT EXISTS Cuadriculas("
                + "CU_Cod VARCHAR(50) NOT NULL, "
                + "S_Cod_Dividido VARCHAR(50) NOT NULL,"
                + "PRIMARY KEY (CU_Cod),"
                + "FOREIGN KEY(S_Cod_Dividido) REFERENCES Sitios(S_Cod))");
        
        query.execute("CREATE TABLE IF NOT EXISTS Cajas("
                + "CA_Cod VARCHAR(50) NOT NULL, "
                + "CA_Fecha VARCHAR(50) NOT NULL,"
                + "CA_Lugar VARCHAR(50) NOT NULL,"
                + "PRIMARY KEY (CA_Cod))");
        
        query.execute("CREATE TABLE IF NOT EXISTS Personas("
                + "P_Dni CHAR(8) NOT NULL, "
                + "P_Nombre VARCHAR(50) NOT NULL,"
                + "P_Apellido VARCHAR(50)  NOT NULL,"
                + "P_Email VARCHAR(50) NOT NULL,"
                + "P_Telefono VARCHAR(50) NOT NULL,"
                + "PRIMARY KEY (P_Dni))");
        
        query.execute("CREATE TABLE IF NOT EXISTS Objetos("
                + "O_Cod VARCHAR(50) NOT NULL, "
                + "O_Nombre VARCHAR(50) NOT NULL,"
                + "O_Tipoextraccion VARCHAR(50) NOT NULL,"
                + "O_Alto INTEGER NOT NULL, "
                + "O_Espesor INTEGER NOT NULL, "
                + "O_Peso INTEGER NOT NULL, "
                + "O_Largo INTEGER NOT NULL, "
                + "O_Cantidad INTEGER NOT NULL, "
                + "O_Fecharegistro VARCHAR(50) NOT NULL, "
                + "O_Descripcion VARCHAR(50) NOT NULL,"
                + "O_Origen VARCHAR(50) NOT NULL,"
                + "O_Es CHAR(1) NOT NULL, "
                + "CU_Cod_Asocia VARCHAR(50) NOT NULL, "
                + "CA_Cod_Contiene VARCHAR(50) NOT NULL, "
                + "P_Dni_Ingresa char(8) NOT NULL,"
                + "PRIMARY KEY (O_Cod),"
                + "FOREIGN KEY(CU_Cod_Asocia) REFERENCES Cuadriculas (CU_Cod),"
                + "FOREIGN KEY(CA_Cod_Contiene) REFERENCES Cajas (CA_Cod),"
                + "FOREIGN KEY(P_Dni_Ingresa) REFERENCES Personas (P_Dni))");

                
        query.execute("CREATE TABLE IF NOT EXISTS Liticos("
                + "L_Fechacreacion INTEGER NOT NULL,"
                + "o_Cod VARCHAR(50) NOT NULL,"
                + "PRIMARY KEY (o_Cod),"
                + "FOREIGN KEY(o_Cod) REFERENCES Objetos(O_Cod))");

        
        query.execute("CREATE TABLE IF NOT EXISTS Ceramicos("
                + "C_Color VARCHAR(50) NOT NULL,"
                + "o_Cod VARCHAR(50) NOT NULL, "
                + "PRIMARY KEY (o_Cod),"
                + "FOREIGN KEY(o_Cod) REFERENCES Objetos(O_Cod))");
        
        precargaBD();
        actualizarPersonas();
        actualizarCajas();
        cantidades();
        maxMinProm();
    }
    
    // FUNCION PARA LEER EL ARCHIVO 
    
    public void precargaBD() throws SQLException{
    // Ruta del archivo SQL a leer
        File archivo = new File("Estado.txt");
        String rutaArchivo = "Inserta_Datos 2023.sql";
        
    // Configuración de la conexión a la base de datos
       conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PWD);
       query = conn.createStatement();
    // Ver si el archivo existe
        if(!archivo.exists()){
            try {

            // Abrir el archivo
                FileReader fileReader = new FileReader(rutaArchivo);
                BufferedReader bufferedReader = new BufferedReader(fileReader);

            // Leer línea por línea
                String linea;
                while ((linea = bufferedReader.readLine()) != null) {
            // Ejecutar el comando SQL
                    query.execute(linea);
                }
            // Agregamos a la persona 
                query.execute("insert into Personas (P_Dni, P_Nombre, P_Apellido, P_Email, P_Telefono) values (25544555, 'Rodolphe', 'Rominov', 'rrominovm@sciencedaily.com', '7135986253')");
            
            // Eliminar al arquelogo 
                query.execute("delete from Personas where (P_Nombre, P_Apellido) values ('Benji', 'Colchett')");
            
            // Cerrar el archivo y la conexión
                bufferedReader.close();
                conn.close();
            
            // Crear el archivo para ver cuadno se ejecuto 
                Files.createFile(Paths.get("Estado.txt"));
            
            } catch (IOException | SQLException e) {
            
            // Manejo de excepciones si ocurre un error al leer el archivo o ejecutar los comandos SQL
                e.printStackTrace();
            }
        }
    }
    
    // Actualizar Persona 
    private void actualizarPersonas() throws SQLException {
        query = conn.createStatement();
        result = query.executeQuery("SELECT * FROM Personas");
        PersonasT.setModel(resultTablas(result));
    }
    // Actualizar Cajas
    private void actualizarCajas() throws SQLException {
        query = conn.createStatement();
        result = query.executeQuery("SELECT * FROM Cajas");
        CajasT.setModel(resultTablas(result));
    }

    private static DefaultTableModel resultTablas(ResultSet rs) throws SQLException {
        // Esta es una función auxiliar que les permite convertir los resultados de las
        // consultas (ResultSet) a un modelo interpretable para la tabla mostrada en pantalla
        // (es decir, para un objeto de tipo JTable, ver línea 81)
        ResultSetMetaData metaData = rs.getMetaData();

        // creando las culmnas de la tabla
        Vector<String> columnNames = new Vector<String>();
        int columnCount = metaData.getColumnCount();
        for (int column = 1; column <= columnCount; column++) {
            columnNames.add(metaData.getColumnName(column));
        }

        // creando las filas de la tabla con los resultados de la consulta
        Vector<Vector<Object>> data = new Vector<Vector<Object>>();
        while (rs.next()) {
            Vector<Object> vector = new Vector<Object>();
            for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
                vector.add(rs.getObject(columnIndex));
            }
            data.add(vector);
        }

        return new DefaultTableModel(data, columnNames);
    }
    
    //FUNCION PARA MOSTRAR CANTIDADES
    private void cantidades() throws SQLException{
        query = conn.createStatement();
        result = query.executeQuery("select count(P_Dni) from Personas");
        CantPersonas.setModel(resultTablas(result));
        
        result = query.executeQuery("select count(CU_Cod) from Cuadriculas");
        CantCuadriculas.setModel(resultTablas(result));
        
        result = query.executeQuery("select count(O_Cod) from Objetos");
        CantObjetos.setModel(resultTablas(result));
        
        result = query.executeQuery("select count(CA_Cod) from Cajas");
        CantCajas.setModel(resultTablas(result));
    }

    //FUNCION PARA MAXIMO, MINIMO Y MEDIA 
    public void maxMinProm() throws SQLException{
        query = conn.createStatement();
        result = query.executeQuery("select max(O_Peso) from Objetos");
        MaxPeso.setModel(resultTablas(result));
        
        query = conn.createStatement();
        result = query.executeQuery("select min(O_Peso) from Objetos");
        MinPeso.setModel(resultTablas(result));
        
        query = conn.createStatement();
        result = query.executeQuery("select max(O_Peso) from Objetos");
        PromPeso.setModel(resultTablas(result));
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        Contenedor = new javax.swing.JPanel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        CajasT = new javax.swing.JTable();
        jScrollPane2 = new javax.swing.JScrollPane();
        PersonasT = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jTextField1 = new javax.swing.JTextField();
        jPanel3 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        CantPersonas = new javax.swing.JTable();
        jScrollPane00 = new javax.swing.JScrollPane();
        CantObjetos = new javax.swing.JTable();
        jScrollPane5 = new javax.swing.JScrollPane();
        CantCajas = new javax.swing.JTable();
        jScrollPane6 = new javax.swing.JScrollPane();
        CantCuadriculas = new javax.swing.JTable();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jPanel8 = new javax.swing.JPanel();
        jPanel9 = new javax.swing.JPanel();
        jPanel10 = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        PromPeso = new javax.swing.JTable();
        jScrollPane7 = new javax.swing.JScrollPane();
        MaxPeso = new javax.swing.JTable();
        jScrollPane8 = new javax.swing.JScrollPane();
        MinPeso = new javax.swing.JTable();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jPanel11 = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        CajasT.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        CajasT.setEnabled(false);
        jScrollPane1.setViewportView(CajasT);

        PersonasT.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        PersonasT.setEnabled(false);
        jScrollPane2.setViewportView(PersonasT);

        jLabel1.setText("                                      Instancia de Personas y Cajas");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap(7, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(64, 64, 64))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 295, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(413, 413, 413))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(134, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Punto 1 ", jPanel1);

        jTextField1.setText("jTextField1");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(54, 54, 54)
                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(867, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(94, 94, 94)
                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(493, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("tab2", jPanel2);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 995, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 611, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("tab3", jPanel3);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 995, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 611, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("tab4", jPanel4);

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 995, Short.MAX_VALUE)
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 611, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("tab5", jPanel5);

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 995, Short.MAX_VALUE)
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 611, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("tab6", jPanel6);

        CantPersonas.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null}
            },
            new String [] {
                "Title 1"
            }
        ));
        CantPersonas.getTableHeader().setReorderingAllowed(false);
        jScrollPane3.setViewportView(CantPersonas);

        CantObjetos.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null}
            },
            new String [] {
                "Title 1"
            }
        ));
        CantObjetos.getTableHeader().setReorderingAllowed(false);
        jScrollPane00.setViewportView(CantObjetos);

        CantCajas.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null}
            },
            new String [] {
                "Title 1"
            }
        ));
        CantCajas.getTableHeader().setReorderingAllowed(false);
        jScrollPane5.setViewportView(CantCajas);

        CantCuadriculas.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null}
            },
            new String [] {
                "Title 1"
            }
        ));
        CantCuadriculas.getTableHeader().setReorderingAllowed(false);
        jScrollPane6.setViewportView(CantCuadriculas);

        jLabel2.setText("Cantidad Personas");

        jLabel3.setText("Cantidad Cuadriculas");

        jLabel4.setText("Cantidad Objetos");

        jLabel5.setText("Cantidad Cajas");

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGap(144, 144, 144)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 69, Short.MAX_VALUE)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addGap(70, 70, 70)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane00, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addGap(78, 78, 78)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5)
                    .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(169, 169, 169))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGap(89, 89, 89)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3)
                    .addComponent(jLabel4)
                    .addComponent(jLabel5))
                .addGap(18, 18, 18)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane00, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(445, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("tab7", jPanel7);

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 995, Short.MAX_VALUE)
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 611, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("tab8", jPanel8);

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 995, Short.MAX_VALUE)
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 611, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("tab9", jPanel9);

        PromPeso.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null}
            },
            new String [] {
                "Title 1"
            }
        ));
        jScrollPane4.setViewportView(PromPeso);

        MaxPeso.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null}
            },
            new String [] {
                "Title 1"
            }
        ));
        jScrollPane7.setViewportView(MaxPeso);

        MinPeso.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null}
            },
            new String [] {
                "Title 1"
            }
        ));
        jScrollPane8.setViewportView(MinPeso);

        jLabel6.setText("Maximo peso de objetos");

        jLabel7.setText("Minimo peso de Objetos");

        jLabel8.setText("Promedio pesos de objetos");

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addGap(139, 139, 139)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6))
                .addGap(133, 133, 133)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane8, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7))
                .addGap(140, 140, 140)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel8)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(148, Short.MAX_VALUE))
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addGap(166, 166, 166)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel6)
                    .addComponent(jLabel7)
                    .addComponent(jLabel8))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane8, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(382, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("tab10", jPanel10);

        javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 995, Short.MAX_VALUE)
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 611, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("tab11", jPanel11);

        javax.swing.GroupLayout ContenedorLayout = new javax.swing.GroupLayout(Contenedor);
        Contenedor.setLayout(ContenedorLayout);
        ContenedorLayout.setHorizontalGroup(
            ContenedorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 997, Short.MAX_VALUE)
        );
        ContenedorLayout.setVerticalGroup(
            ContenedorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(Contenedor, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(Contenedor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            try {
                new MainLab().setVisible(true);
            } catch (SQLException ex) {
                Logger.getLogger(MainLab.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable CajasT;
    private javax.swing.JTable CantCajas;
    private javax.swing.JTable CantCuadriculas;
    private javax.swing.JTable CantObjetos;
    private javax.swing.JTable CantPersonas;
    private javax.swing.JPanel Contenedor;
    private javax.swing.JTable MaxPeso;
    private javax.swing.JTable MinPeso;
    private javax.swing.JTable PersonasT;
    private javax.swing.JTable PromPeso;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane00;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextField jTextField1;
    // End of variables declaration//GEN-END:variables
}
