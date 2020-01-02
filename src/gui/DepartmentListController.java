package gui;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import application.Main;
import db.DbException;
import gui.listeners.DataChangeListener;
import gui.util.Alerts;
import gui.util.Utils;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.entities.Department;
import model.services.DepartmentService;

public class DepartmentListController implements Initializable, DataChangeListener {

	private DepartmentService depService;
	private ObservableList<Department> obsList;

	@FXML
	private TableView<Department> tableViewDepartment;
	@FXML
	private TableColumn<Department, Integer> tableColumnId;
	@FXML
	private TableColumn<Department, String> tableColumnName;
	@FXML
	private TableColumn<Department, Department> tableColumnEDIT;
	@FXML
	private TableColumn<Department, Department> tableColumnREMOVE;
	@FXML
	private Button btNew;

	@FXML
	public void onBtNewAction(ActionEvent event) {
		Stage parentStage = Utils.currentStage(event);
		Department dep = new Department(null, "");
		createDialogForm(dep, depService, "/gui/DepartmentForm.fxml", parentStage);
	}

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		initializeNodes();
	}

	private void initializeNodes() {
		tableColumnId.setCellValueFactory(new PropertyValueFactory<>("id"));
		tableColumnName.setCellValueFactory(new PropertyValueFactory<>("name"));

		Stage stage = (Stage) Main.getMainScene().getWindow();
		tableViewDepartment.prefHeightProperty().bind(stage.heightProperty());

	}

	public void setDepartmentService(DepartmentService depService) {
		this.depService = depService;
	}

	public void initEditButtons() {
		tableColumnEDIT.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
		tableColumnEDIT.setCellFactory(param -> new TableCell<Department, Department>() {
			private final Button button = new Button("edit");

			@Override
			protected void updateItem(Department department, boolean empty) {
				super.updateItem(department, empty);
				if (department == null) {
					setGraphic(null);
					return;
				}

				setGraphic(button);
				button.setOnAction(event -> createDialogForm(department, depService, "/gui/DepartmentForm.fxml",
						Utils.currentStage(event)));
			}
		});
	}

	public void initRemoveButtons() {
		tableColumnREMOVE.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
		tableColumnREMOVE.setCellFactory(param -> new TableCell<Department, Department>() {
			private final Button button = new Button("remove");

			@Override
			protected void updateItem(Department department, boolean empty) {
				super.updateItem(department, empty);
				if (department == null) {
					setGraphic(null);
					return;
				}

				setGraphic(button);
				button.setOnAction(event -> removeEntity(department));
			}
		});
	}

	private void removeEntity(Department department) {
		Optional<ButtonType> result = Alerts.showConfirmation("Confirmation", "Area you sure to delete?");
		if (result.get() == ButtonType.OK) {
			if (depService == null) {
				throw new IllegalStateException("Service was null");
			}
			try {
				depService.remove(department);
				updateTableView();
			} catch (DbException e) {
				Alerts.showAlert("Error removing object", null, e.getMessage(), AlertType.ERROR);
			}
		}
	}

	public void updateTableView() {
		if (depService == null) {
			throw new IllegalStateException("Service was null");
		}
		List<Department> list = depService.findAll();
		obsList = FXCollections.observableArrayList(list);
		tableViewDepartment.setItems(obsList);
		initEditButtons();
		initRemoveButtons();
	}

	private void createDialogForm(Department dep, DepartmentService service, String absolutePath, Stage parentStage) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource(absolutePath));
			Pane pane = loader.load();

			DepartmentFormController formController = loader.getController();
			formController.setDepartment(dep);
			formController.setDepartmentService(service);
			formController.subscribeDataChangeListener(this);
			formController.updateFormData();

			Stage dialogStage = new Stage();
			dialogStage.setTitle("Enter department data");
			dialogStage.setScene(new Scene(pane));
			dialogStage.setResizable(false);
			dialogStage.initOwner(parentStage);
			dialogStage.initModality(Modality.WINDOW_MODAL);
			dialogStage.showAndWait();
		} catch (IOException e) {
			Alerts.showAlert("IO Exception", "Error loading view", e.getMessage(), AlertType.ERROR);
		}
	}

	@Override
	public void onDataChanged() {
		updateTableView();
	}

}
