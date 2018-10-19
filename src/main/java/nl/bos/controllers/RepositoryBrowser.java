package nl.bos.controllers;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import lombok.extern.java.Log;
import nl.bos.MyTreeItem;
import nl.bos.Repository;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

@Log
public class RepositoryBrowser implements Initializable, ChangeListener<TreeItem<MyTreeItem>> {
    @FXML
    private TreeView<MyTreeItem> treeview;
    private Repository repositoryCon = Repository.getInstance();

    @FXML
    private void handleExit(ActionEvent actionEvent) {
        log.info(String.valueOf(actionEvent.getSource()));
        Stage browseRepositoryStage = RootPane.getBrowseRepositoryStage();
        browseRepositoryStage.fireEvent(new WindowEvent(browseRepositoryStage, WindowEvent.WINDOW_CLOSE_REQUEST));
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        log.info(String.valueOf(location));
        MyTreeItem rootItem = new MyTreeItem(repositoryCon.getRepositoryName(), "repository", "");
        treeview.setRoot(buildFileSystemBrowser(rootItem));
        treeview.getSelectionModel().selectedItemProperty().addListener(this);
    }

    private TreeItem<MyTreeItem> buildFileSystemBrowser(MyTreeItem treeItem) {
        return createNode(treeItem);
    }

    // This method creates a TreeItem to represent the given File. It does this
    // by overriding the TreeItem.getChildren() and TreeItem.isLeaf() methods
    // anonymously, but this could be better abstracted by creating a
    // 'FileTreeItem' subclass of TreeItem. However, this is left as an exercise
    // for the reader.
    private TreeItem<MyTreeItem> createNode(final MyTreeItem treeItem) {
        return new TreeItem<MyTreeItem>(treeItem, new ImageView(new Image(getClass().getClassLoader().getResourceAsStream(String.format("%s_16.png", treeItem.getType()))))) {
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
            private boolean isFirstTimeChildren = true;
            private boolean isFirstTimeLeaf = true;

            @Override
            public ObservableList<TreeItem<MyTreeItem>> getChildren() {
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
                    MyTreeItem treeItem = getValue();
                    isLeaf = !treeItem.isDirectory();
                }
                return isLeaf;
            }

            private ObservableList<TreeItem<MyTreeItem>> buildChildren(TreeItem<MyTreeItem> parent) {
                MyTreeItem treeItem = parent.getValue();
                if (treeItem != null && treeItem.isDirectory()) {
                    List<MyTreeItem> treeItems = treeItem.listObjects(treeItem);
                    if (treeItems != null) {
                        ObservableList<TreeItem<MyTreeItem>> children = FXCollections.observableArrayList();
                        for (MyTreeItem childFile : treeItems) {
                            children.add(createNode(childFile));
                        }
                        return children;
                    }
                }
                return FXCollections.emptyObservableList();
            }
        };
    }

    @Override
    public void changed(ObservableValue<? extends TreeItem<MyTreeItem>> observable, TreeItem<MyTreeItem> oldValue, TreeItem<MyTreeItem> newValue) {
        log.info(String.format("Selected item: %s", newValue.getValue().getName()));
    }
}
