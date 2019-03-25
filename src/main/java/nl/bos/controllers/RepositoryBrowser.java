package nl.bos.controllers;

import com.documentum.fc.client.DfACL;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.common.DfException;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import nl.bos.BrowserTreeItem;
import nl.bos.Constants;
import nl.bos.Repository;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static nl.bos.Constants.*;

public class RepositoryBrowser implements ChangeListener<TreeItem<BrowserTreeItem>>, EventHandler<ActionEvent> {
    private static final Logger LOGGER = Logger.getLogger(RepositoryBrowser.class.getName());

    private final Repository repositoryCon = Repository.getInstance();

    @FXML
    private TreeView<BrowserTreeItem> treeView;
    @FXML
    private TextField txtObjectId;
    @FXML
    private TextField txtObjectType;
    @FXML
    private TextField txtContentType;
    @FXML
    private TextField txtContentSize;
    @FXML
    private TextField txtCreationDate;
    @FXML
    private TextField txtModifyDate;
    @FXML
    private TextField txtLockOwner;
    @FXML
    private TextField txtLockMachine;
    @FXML
    private TextField txtLockDate;
    @FXML
    private TextField txtAclName;
    @FXML
    private TextField txtPermission;
    @FXML
    private TextField txtVersion;
    @FXML
    private Button btnExit;
    @FXML
    private Label lblNrOfItems;
    @FXML
    private CheckBox ckbShowAllCabinets;
    @FXML
    private CheckBox ckbShowAllVersions;

    private BrowserTreeItem rootItem;
    private MyTreeNode selected;
    private final ContextMenu rootContextMenu = new ContextMenu();

    @FXML
    private void initialize() {
        MenuItem miDump = new MenuItem("Get Attributes");
        miDump.setOnAction(this);
        rootContextMenu.getItems().add(miDump);

        rootItem = new BrowserTreeItem(null, repositoryCon.getRepositoryName(), TYPE_REPOSITORY, "");
        TreeItem<BrowserTreeItem> treeItemBrowser = buildTreeItemBrowser(rootItem);
        treeItemBrowser.setExpanded(true);
        treeView.setRoot(treeItemBrowser);
        treeView.getSelectionModel().selectedItemProperty().addListener(this);
        treeView.addEventHandler(MouseEvent.MOUSE_RELEASED, mouseEvent -> {
            LOGGER.finest(String.format("Click-count: %s", String.valueOf(mouseEvent.getClickCount())));
            selected = (MyTreeNode) treeView.getSelectionModel().getSelectedItem();
            if (selected != null && !selected.isExpanded())
                selected.isFirstTimeChildren = true;
            if (mouseEvent.getButton() == MouseButton.SECONDARY) {
                selected = (MyTreeNode) treeView.getSelectionModel().getSelectedItem();
                //item is selected - this prevents fail when clicking on empty space
                if (selected != null && !selected.getValue().getType().equals(TYPE_REPOSITORY)) {
                    //open context menu on current screen position
                    rootContextMenu.show(treeView, mouseEvent.getScreenX(), mouseEvent.getScreenY());
                }
            } else {
                //any other click cause hiding menu
                rootContextMenu.hide();
            }
        });
    }


    private TreeItem<BrowserTreeItem> buildTreeItemBrowser(BrowserTreeItem treeItem) {
        return createNode(treeItem);
    }

    // This method creates a TreeItem to represent the given File. It does this
    // by overriding the TreeItem.getChildren() and TreeItem.isLeaf() methods
    // anonymously, but this could be better abstracted by creating a
    // 'FileTreeItem' subclass of TreeItem. However, this is left as an exercise
    // for the reader.
    private TreeItem<BrowserTreeItem> createNode(final BrowserTreeItem treeItem) {
        Image image = new Image(getClass().getClassLoader().getResourceAsStream(String.format("nl/bos/icons/type/t_%s_16.gif", treeItem.getType())));
        try {
            if (treeItem.getType().equals(TYPE_CABINET)) {
                boolean isPrivate = treeItem.getObject().getBoolean(ATTR_IS_PRIVATE);
                if (isPrivate)
                    image = new Image(getClass().getClassLoader().getResourceAsStream("nl/bos/icons/type/t_mycabinet_16.gif"));
            } else if (treeItem.getType().equals(TYPE_DOCUMENT)) {
                String lockOwner = treeItem.getObject().getString(Constants.ATTR_R_LOCK_OWNER);
                if (!lockOwner.equals(""))
                    image = new Image(getClass().getClassLoader().getResourceAsStream("nl/bos/icons/type/t_dm_document_lock_16.gif"));
            }

        } catch (DfException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
        ImageView imageView = new ImageView(image);
        return new MyTreeNode(treeItem, imageView);
    }

    @Override
    public void changed(ObservableValue<? extends TreeItem<BrowserTreeItem>> observable, TreeItem<BrowserTreeItem> oldValue, TreeItem<BrowserTreeItem> newValue) {
        BrowserTreeItem selectedItem = newValue.getValue();
        LOGGER.info(String.format("Selected item: %s", selectedItem.getName()));
        IDfPersistentObject selectedObject = selectedItem.getObject();
        if (selectedObject != null) {
            try {
                txtObjectId.setText(selectedObject.getObjectId().getId());
                txtObjectType.setText(selectedObject.getType().getName());
                txtContentType.setText(selectedObject.getString(ATTR_A_CONTENT_TYPE));
                txtContentSize.setText(selectedObject.getString(ATTR_R_CONTENT_SIZE));
                txtCreationDate.setText(selectedObject.getTime(ATTR_R_CREATION_DATE).asString(""));
                txtModifyDate.setText(selectedObject.getTime(ATTR_R_MODIFY_DATE).asString(""));
                txtLockOwner.setText(selectedObject.getString(ATTR_R_LOCK_OWNER));
                txtLockMachine.setText(selectedObject.getString(ATTR_R_LOCK_MACHINE));
                txtLockDate.setText(selectedObject.getTime(ATTR_R_LOCK_DATE).asString(""));
                txtAclName.setText(selectedObject.getString(ATTR_ACL_NAME));
                txtPermission.setText(convertPermitToLabel(selectedObject.getInt(ATTR_OWNER_PERMIT)));
                txtVersion.setText(getRepeatingValue(selectedObject));
            } catch (DfException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            }
        } else {
            txtObjectId.setText("");
            txtObjectType.setText("");
            txtContentType.setText("");
            txtContentSize.setText("");
            txtCreationDate.setText("");
            txtModifyDate.setText("");
            txtLockOwner.setText("");
            txtLockMachine.setText("");
            txtLockDate.setText("");
            txtAclName.setText("");
            txtPermission.setText("");
            txtVersion.setText("");
        }
    }

    private String getRepeatingValue(IDfPersistentObject object) throws DfException {
        int count = object.getValueCount(ATTR_R_VERSION_LABEL);
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < count; i++) {
            result.append(object.getRepeatingString(ATTR_R_VERSION_LABEL, i));
            if (i != count - 1)
                result.append(", ");
        }
        return String.valueOf(result);
    }

    private String convertPermitToLabel(int permit) {
        if (permit == DfACL.DF_PERMIT_NONE)
            return DfACL.DF_PERMIT_NONE_STR;
        if (permit == DfACL.DF_PERMIT_BROWSE)
            return DfACL.DF_PERMIT_BROWSE_STR;
        if (permit == DfACL.DF_PERMIT_READ)
            return DfACL.DF_PERMIT_READ_STR;
        if (permit == DfACL.DF_PERMIT_RELATE)
            return DfACL.DF_PERMIT_RELATE_STR;
        if (permit == DfACL.DF_PERMIT_VERSION)
            return DfACL.DF_PERMIT_VERSION_STR;
        if (permit == DfACL.DF_PERMIT_WRITE)
            return DfACL.DF_PERMIT_WRITE_STR;
        if (permit == DfACL.DF_PERMIT_DELETE)
            return DfACL.DF_PERMIT_DELETE_STR;
        return "";
    }

    @Override
    public void handle(ActionEvent event) {
        try {
            LOGGER.info(selected.getValue().getObject().getObjectId().getId());
            Stage dumpAttributes = new Stage();
            dumpAttributes.setTitle(String.format("Attributes List - %s (%s)", selected.getValue().getObject().getObjectId().getId(), repositoryCon.getRepositoryName()));
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/nl/bos/views/GetAttributes.fxml"));
            VBox loginPane = fxmlLoader.load();
            Scene scene = new Scene(loginPane);
            dumpAttributes.setScene(scene);
            GetAttributes controller = fxmlLoader.getController();
            controller.initTextArea(selected.getValue().getObject());
            dumpAttributes.showAndWait();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    @FXML
    private void handleExit(ActionEvent actionEvent) {
        LOGGER.info(String.valueOf(actionEvent.getSource()));
        Stage stage = (Stage) btnExit.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleShowAllCabinets(ActionEvent actionEvent) {
        TreeItem<BrowserTreeItem> treeItemBrowser = buildTreeItemBrowser(rootItem);
        treeItemBrowser.setExpanded(true);
        treeView.setRoot(treeItemBrowser);
    }

    private class MyTreeNode extends TreeItem<BrowserTreeItem> {
        // We cache whether the File is a leaf or not. A File is a leaf if
        // it is not a directory and does not have any files contained within
        // it. We cache this as isLeaf() is called often, and doing the
        // actual check on File is expensive.
        private boolean isLeaf;

        // We do the children and leaf testing only once, and then set these
        // booleans to false so that we do not check again during this
        // run. A more complete implementation may need to handle more
        // dynamic file system situations (such as where a folder has files
        // added after the TreeView is shown). Again, this is left as an
        // exercise for the reader.
        private boolean isFirstTimeChildren;
        private boolean isFirstTimeLeaf;

        private MyTreeNode(BrowserTreeItem treeItem, ImageView imageView) {
            super(treeItem, imageView);
            isFirstTimeChildren = true;
            isFirstTimeLeaf = true;
        }

        @Override
        public ObservableList<TreeItem<BrowserTreeItem>> getChildren() {
            if (isFirstTimeChildren) {
                isFirstTimeChildren = false;

                // First getChildren() call, so we actually go off and
                // determine the children of the File contained in this TreeItem.
                super.getChildren().setAll(buildChildren(this));
            }
            return super.getChildren();
        }

        @Override
        public boolean isLeaf() {
            if (isFirstTimeLeaf) {
                isFirstTimeLeaf = false;
                BrowserTreeItem treeItem = getValue();
                isLeaf = !treeItem.isDirectory();
            }
            return isLeaf;
        }

        private ObservableList<TreeItem<BrowserTreeItem>> buildChildren(MyTreeNode parent) {
            BrowserTreeItem parentItem = parent.getValue();
            if (parentItem != null && parentItem.isDirectory()) {
                List<BrowserTreeItem> treeItems = parentItem.listObjects(parentItem, ckbShowAllCabinets.isSelected(), ckbShowAllVersions.isSelected());
                lblNrOfItems.setText(String.format("%s items found", treeItems.size()));
                ObservableList<TreeItem<BrowserTreeItem>> children = FXCollections.observableArrayList();
                for (BrowserTreeItem treeItem : treeItems) {
                    children.add(createNode(treeItem));
                }
                return children;
            }
            return FXCollections.emptyObservableList();
        }
    }
}