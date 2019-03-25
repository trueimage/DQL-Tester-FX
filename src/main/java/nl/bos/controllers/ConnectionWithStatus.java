package nl.bos.controllers;

import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfUser;
import com.documentum.fc.common.DfException;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import nl.bos.Main;
import nl.bos.Repository;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConnectionWithStatus implements EventHandler<WindowEvent> {
    private static final Logger LOGGER = Logger.getLogger(ConnectionWithStatus.class.getName());

    private final Repository repositoryCon = Repository.getInstance();
    private final Main main = Main.getInstance();

    private final static Stage loginStage = new Stage();
    private final FXMLLoader fxmlLoader;

    @FXML
    private Label lblStatus;
    @FXML
    private Label lblUsernameOS;
    @FXML
    private Label lblUsernameDC;
    @FXML
    private Label lblDomainOS;
    @FXML
    private Label lblPrivileges;
    @FXML
    private Label lblServerVersion;
    @FXML
    private Button btnFlushCache;
    @FXML
    private Button btnReadQuery;
    @FXML
    private Button btnConnect;
    @FXML
    private Button btnDisconnect;
    @FXML
    private Tooltip ttStatus;
    @FXML
    private Tooltip ttOSUsername;
    @FXML
    private Tooltip ttDCUsername;
    @FXML
    private Tooltip ttDomain;
    @FXML
    private Tooltip ttPrivileges;
    @FXML
    private Tooltip ttServerVersion;

    static Stage getLoginStage() {
        return loginStage;
    }

    public Button getBtnFlushCache() {
        return btnFlushCache;
    }

    public Button getBtnReadQuery() {
        return btnReadQuery;
    }

    public Button getBtnConnect() {
        return btnConnect;
    }

    public Button getBtnDisconnect() {
        return btnDisconnect;
    }

    /**
     * @noinspection WeakerAccess
     */
    public ConnectionWithStatus() {
        loginStage.initModality(Modality.APPLICATION_MODAL);
        loginStage.setTitle("Documentum Login");
        fxmlLoader = new FXMLLoader(getClass().getResource("/nl/bos/views/Login.fxml"));

        try {
            VBox loginPane = fxmlLoader.load();
            loginStage.setScene(new Scene(loginPane));
            loginStage.setOnCloseRequest(this);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    @FXML
    private void initialize() {
        btnDisconnect.managedProperty().bindBidirectional(btnDisconnect.visibleProperty());
        btnDisconnect.setManaged(false);
    }

    public void handle(WindowEvent event) {
        IDfSession session = repositoryCon.getSession();
        if (session != null && session.isConnected()) {
            try {
                updateNodes(session);
            } catch (DfException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            }
        } else {
            updateNodes();
        }
    }

    public void updateNodes(IDfSession session) throws DfException {
        IDfUser user = session.getUser(session.getLoginUserName());

        lblStatus.setText(session.getDocbaseName());
        ttStatus.setText(String.format("Connected repository: %s\nRepository hostname: %s\nRepository ID: %s\nConnection Broker hostname: %s\nConnection Broker port: %s", session.getDocbaseName(), repositoryCon.obtainServerMap(session.getDocbaseName()).getString("i_host_name"), session.getDocbaseId(), repositoryCon.obtainRepositoryMap().getRepeatingString("i_host_name", 0), repositoryCon.obtainRepositoryMap().getRepeatingString("i_port_number", 0)));

        lblUsernameOS.setText(user.getUserOSName());
        ttOSUsername.setText(String.format("Operating System Username: %s\nDefault Folder: %s", user.getUserOSName(), user.getDefaultFolder()));

        lblUsernameDC.setText(session.getLoginUserName());
        ttDCUsername.setText(String.format("Documentum Username %s\nDefault Group: %s\nSession ID: %s\nAddress: %s", session.getLoginUserName(), user.getUserGroupName(), user.getObjectSession().getSessionId(), user.getUserAddress()));

        lblDomainOS.setText(user.getUserOSDomain());
        ttDomain.setText(String.format("Operation System Domain: %s", user.getUserOSDomain()));

        lblPrivileges.setText(String.format("%s (%d)", getUserPrivilegesLabel(user.getUserPrivileges()), user.getUserPrivileges()));
        ttPrivileges.setText(String.format("Documentum User Privileges: %s (%d)\nClient Capability: %s (%d)", getUserPrivilegesLabel(user.getUserPrivileges()), user.getUserPrivileges(), getClientCapabilityLabel(user.getClientCapability()), user.getClientCapability()));

        lblServerVersion.setText(session.getServerVersion());
        ttServerVersion.setText(String.format("Documentum Server Version: %s \nConnection Broker Version: %s", session.getServerVersion(), repositoryCon.obtainServerMap(session.getDocbaseName()).getString("i_docbroker_version")));

        btnReadQuery.setDisable(!session.isConnected());
        btnFlushCache.setDisable(!session.isConnected());

        btnDisconnect.managedProperty().bindBidirectional(btnDisconnect.visibleProperty());
        btnDisconnect.setManaged(session.isConnected());

        btnConnect.managedProperty().bindBidirectional(btnConnect.visibleProperty());
        btnConnect.setManaged(!session.isConnected());

        Menu menuLoaderController = main.getRootPaneLoader().getController();
        menuLoaderController.getMenubar().setDisable(!session.isConnected());
    }

    private String getClientCapabilityLabel(int clientCapability) {
        Map<Integer, String> clientCapabilityLabels = new HashMap<>();
        clientCapabilityLabels.put(0, "Consumer");
        clientCapabilityLabels.put(1, "Consumer");
        clientCapabilityLabels.put(2, "Contributor");
        clientCapabilityLabels.put(4, "Coordinator");
        clientCapabilityLabels.put(8, "System Administrator");

        return clientCapabilityLabels.getOrDefault(clientCapability, "");
    }

    private String getUserPrivilegesLabel(int userPrivileges) {
        Map<Integer, String> userPrivilegeLabels = new HashMap<>();
        userPrivilegeLabels.put(0, "None");
        userPrivilegeLabels.put(1, "Create Type");
        userPrivilegeLabels.put(2, "Create Cabinet");
        userPrivilegeLabels.put(4, "Create Group");
        userPrivilegeLabels.put(8, "Sysadmin");
        userPrivilegeLabels.put(16, "Superuser");

        return userPrivilegeLabels.getOrDefault(userPrivileges, "");
    }

    private void updateNodes() {
        lblStatus.setText("Offline");
        lblUsernameOS.setText("OS Username");
        lblUsernameDC.setText("DC Username");
        lblDomainOS.setText("OS Domain");
        lblPrivileges.setText("Privileges");
        lblServerVersion.setText("Server Version");

        btnReadQuery.setDisable(true);
        btnFlushCache.setDisable(true);

        btnDisconnect.managedProperty().bindBidirectional(btnDisconnect.visibleProperty());
        btnDisconnect.setManaged(false);

        btnConnect.managedProperty().bindBidirectional(btnConnect.visibleProperty());
        btnConnect.setManaged(true);

        Menu menuLoaderController = main.getRootPaneLoader().getController();
        menuLoaderController.getMenubar().setDisable(true);
    }

    @FXML
    private void handleConnect(ActionEvent actionEvent) {
        LOGGER.info(String.valueOf(actionEvent.getSource()));
        repositoryCon.setClient();
        Login loginController = fxmlLoader.getController();
        loginController.initialize();
        loginStage.showAndWait();
    }

    @FXML
    private void handleExit(ActionEvent actionEvent) {
        LOGGER.info(String.valueOf(actionEvent.getSource()));

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Quit the application...");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            repositoryCon.disconnect();
            System.exit(0);
        } else {
            alert.close();
        }
    }

    @FXML
    private void handleReadQuery(ActionEvent actionEvent) {
        LOGGER.info(String.valueOf(actionEvent.getSource()));

        QueryWithResult queryWithResultController = main.getBodyPaneLoader().getController();
        String statement = queryWithResultController.getStatement().getText();
        JSONObject jsonObject = queryWithResultController.getJsonObject();

        IDfCollection result = repositoryCon.query(statement);
        if (result != null) {
            try {
                queryWithResultController.updateResultTable(result);
                result.close();
            } catch (DfException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            }

            ChoiceBox<Object> cmbHistory = queryWithResultController.getHistoryStatements();
            ObservableList<Object> items = cmbHistory.getItems();
            if (statementNotExists(items, statement)) {
                items.add(0, statement);
                cmbHistory.setValue(statement);
                JSONArray queries = (JSONArray) jsonObject.get("queries");
                if (queries.length() > 0) {
                    queries.put(queries.get(0));
                }
                queries.put(0, statement);
                try (FileWriter file = new FileWriter("history.json")) {
                    file.write(jsonObject.toString());
                    file.flush();
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                }
                cmbHistory.setItems(items);
            }
        }
    }

    private boolean statementNotExists(ObservableList items, String statement) {
        for (Object item : items) {
            String historyStatement = (String) item;
            if (historyStatement.equalsIgnoreCase(statement))
                return false;
        }
        return true;
    }

    @FXML
    private void handleClearQuery(ActionEvent actionEvent) {
        QueryWithResult queryWithResultController = main.getBodyPaneLoader().getController();
        queryWithResultController.getStatement().clear();
    }

    @FXML
    private void handleDisconnect(ActionEvent actionEvent) {
        Login loginController = fxmlLoader.getController();
        loginController.initialize();
        loginStage.showAndWait();
    }
}