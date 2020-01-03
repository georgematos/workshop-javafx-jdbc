package gui;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
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
import model.entities.Seller;
import model.services.SellerService;

public class SellerListController implements Initializable, DataChangeListener {

	private SellerService sellerService;
	private ObservableList<Seller> obsList;

	@FXML
	private TableView<Seller> tableViewSeller;
	@FXML
	private TableColumn<Seller, Integer> tableColumnId;
	@FXML
	private TableColumn<Seller, String> tableColumnName;
	@FXML
	private TableColumn<Seller, String> tableColumnEmail;
	@FXML
	private TableColumn<Seller, Date> tableColumnBirthDate;
	@FXML
	private TableColumn<Seller, Double> tableColumnBaseSalary;
	@FXML
	private TableColumn<Seller, Department> tableColumnDepartment;
	@FXML
	private TableColumn<Seller, Seller> tableColumnEDIT;
	@FXML
	private TableColumn<Seller, Seller> tableColumnREMOVE;
	@FXML
	private Button btNew;

	@FXML
	public void onBtNewAction(ActionEvent event) {
		Stage parentStage = Utils.currentStage(event);
		Seller seller = new Seller();
		createDialogForm(seller, sellerService, "/gui/SellerForm.fxml", parentStage);
	}

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		initializeNodes();
	}

	private void initializeNodes() {
		tableColumnId.setCellValueFactory(new PropertyValueFactory<>("id"));
		tableColumnName.setCellValueFactory(new PropertyValueFactory<>("name"));
		tableColumnEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
		tableColumnBirthDate.setCellValueFactory(new PropertyValueFactory<>("birthDate"));
		tableColumnBaseSalary.setCellValueFactory(new PropertyValueFactory<>("baseSalary"));
		tableColumnDepartment.setCellValueFactory(new PropertyValueFactory<>("department"));
		Utils.formatTableColumnDate(tableColumnBirthDate, "dd/MM/yyyy");
		Utils.formatTableColumnDouble(tableColumnBaseSalary, 2);
		Utils.formatTableColumnDepartment(tableColumnDepartment);

		Stage stage = (Stage) Main.getMainScene().getWindow();
		tableViewSeller.prefHeightProperty().bind(stage.heightProperty());

	}

	public void setSellerService(SellerService sellerService) {
		this.sellerService = sellerService;
	}

	public void initEditButtons() {
		tableColumnEDIT.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
		tableColumnEDIT.setCellFactory(param -> new TableCell<Seller, Seller>() {
			private final Button button = new Button("edit");

			@Override
			protected void updateItem(Seller seller, boolean empty) {
				super.updateItem(seller, empty);
				if (seller == null) {
					setGraphic(null);
					return;
				}

				setGraphic(button);
				button.setOnAction(event -> createDialogForm(seller, sellerService, "/gui/SellerForm.fxml",
						Utils.currentStage(event)));
			}
		});
	}

	public void initRemoveButtons() {
		tableColumnREMOVE.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
		tableColumnREMOVE.setCellFactory(param -> new TableCell<Seller, Seller>() {
			private final Button button = new Button("remove");

			@Override
			protected void updateItem(Seller seller, boolean empty) {
				super.updateItem(seller, empty);
				if (seller == null) {
					setGraphic(null);
					return;
				}

				setGraphic(button);
				button.setOnAction(event -> removeEntity(seller));
			}
		});
	}

	private void removeEntity(Seller seller) {
		Optional<ButtonType> result = Alerts.showConfirmation("Confirmation", "Area you sure to delete?");
		if (result.get() == ButtonType.OK) {
			if (sellerService == null) {
				throw new IllegalStateException("Service was null");
			}
			try {
				sellerService.remove(seller);
				updateTableView();
			} catch (DbException e) {
				Alerts.showAlert("Error removing object", null, e.getMessage(), AlertType.ERROR);
			}
		}
	}

	public void updateTableView() {
		if (sellerService == null) {
			throw new IllegalStateException("Service was null");
		}
		List<Seller> list = sellerService.findAll();
		obsList = FXCollections.observableArrayList(list);
		tableViewSeller.setItems(obsList);
		initEditButtons();
		initRemoveButtons();
	}

	private void createDialogForm(Seller seller, SellerService service, String absolutePath, Stage parentStage) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource(absolutePath));
			Pane pane = loader.load();

			SellerFormController formController = loader.getController();
			formController.setSeller(seller);
			formController.setSellerService(service);
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
