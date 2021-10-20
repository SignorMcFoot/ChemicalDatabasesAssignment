package DatabaseConnection;
import java.sql.*;

//The main connector class based on GH example
public class Connect
{
    Connection conn = null;
    private String username = "root";
    private String password;
    private String host ;

    ResultSet resSet;

    public Connect(String host, String password){
        this.host = host;
        this.password = password;
        connect(host);
    }
    //Updated method from Graham's code as the solution was deprecated in the newer version of Java
    private void connect(String url) {
        try {
            conn = DriverManager.getConnection(url, username, password);
        } catch(SQLException e)
        {
            System.out.println("Trying to connect to MySQL " + e);
        }
    }

    // Allows user to construct prepared query [1]
    public Connection getConnection(){
        return conn;
    }

    // Takes PreparedStatement to run query [1]
    public ResultSet runPreparedQuery(PreparedStatement stmt){
        try{
            resSet = stmt.executeQuery();
        }
        catch(SQLException e){
            System.out.println("Trying to make query" + e.toString());
        }
        return resSet;
    }
}
//[1] Taken from Graham's code