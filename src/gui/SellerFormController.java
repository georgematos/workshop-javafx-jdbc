package gui;

import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import db.DbException;
import gui.listeners.DataChangeListener;
import gui.util.Alerts;
import gui.util.Constraints;
import gui.util.Utils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.util.Callback;
import model.entities.Department;
import model.entities.Seller;
import model.exceptions.ValidationException;
import model.services.DepartmentService;
import model.services.SellerService;

public class SellerFormController implements Initializable {

	private Seller seller;
	private SellerService service;
	private DepartmentService depService;
	private List<DataChangeListener> dataChangeListeners = new ArrayList<>();

	@FXML
	private GridPane gridPane;
	@FXML
	private TextField txtFieldId;
	@FXML
	private TextField txtFieldName;
	@FXML
	private Label labelErrorName;
	@FXML
	private TextField txtFieldEmail;
	@FXML
	private Label labelErrorEmail;
	@FXML
	private DatePicker datePickerBirthDate;
	@FXML
	private Label labelErrorBirthDate;
	@FXML
	private TextField txtFieldBaseSalary;
	@FXML
	private Label labelErrorBaseSalary;
	@FXML
	private ComboBox<Department> comboBoxDepartment;
	@FXML
	private Label labelErrorDepartment;
	@FXML
	private Button btnSave;
	@FXML
	private Button btnCancel;

	private ObservableList<Department> obsList;

	public void loadAssociateObjects() {
		if (depService == null) {
			throw new IllegalStateException("Department Service was null");
		}
		List<Department> list = depService.findAll();
		obsList = FXCollections.observableArrayList(list);
		comboBoxDepartment.setItems(obsList);
	}

	public void setSeller(Seller seller) {
		this.seller = seller;
	}

	public void setServices(SellerService service, DepartmentService depService) {
		this.service = service;
		this.depService = depService;
	}

	public void subscribeDataChangeListener(DataChangeListener listener) {
		dataChangeListeners.add(listener);
	}

	@FXML
	public void onBtSaveAction(ActionEvent event) {
		if (seller == null) {
			throw new IllegalStateException("Entity was null");
		}
		if (service == null) {
			throw new IllegalStateException("Service was null");
		}
		try {
			seller = getFormData();
			service.saveOrUpdate(seller);
			notifyDataChangeListeners();
			Utils.currentStage(event).close();
		} catch (ValidationException e) {
			setErrorMessages(e.getErrors());
		} catch (DbException e) {
			Alerts.showAlert("Error saving object", null, e.getMessage(), AlertType.ERROR);
		}
	}

	private void notifyDataChangeListeners() {
		for (DataChangeListener listener : dataChangeListeners) {
			listener.onDataChanged();
		}
	}

	private Seller getFormData() {
		String txtName = txtFieldName.getText();
		ValidationException exception = new ValidationException("Validation Exception");
		if (txtName.isEmpty()) {
			exception.addError("name", "Name field is empty");
		}
		if (exception.getErrors().size() > 0) {
			throw exception;
		}
		Seller seller = new Seller();
		seller.setId(Utils.tryParseToInt((txtFieldId.getText())));
		seller.setName(txtName);
		return seller;
	}

	@FXML
	public void onBtCancelAction(ActionEvent event) {
		Utils.currentStage(event).close();
	}

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		initializeNodes();
	}

	private void initializeNodes() {
		Constraints.setTextFieldInteger(txtFieldId);
		Constraints.setTextFieldMaxLength(txtFieldName, 30);
		Constraints.setTextFieldDouble(txtFieldBaseSalary);
		Constraints.setTextFieldMaxLength(txtFieldEmail, 60);
		Utils.formatDatePicker(datePickerBirthDate, "dd/MM/yyyy");
		initializeComboBoxDepartment();
	}

	public void updateFormData() {
		if (seller == null) {
			throw new IllegalStateException("Entity was null");
		}
		txtFieldId.setText(String.valueOf(seller.getId()));
		txtFieldName.setText(seller.getName());
		txtFieldEmail.setText(seller.getEmail());
		Locale.setDefault(Locale.US);
		if (seller.getBirthDate() != null) {
			datePickerBirthDate
					.setValue(LocalDate.ofInstant(seller.getBirthDate().toInstant(), ZoneId.systemDefault()));
		}
		txtFieldBaseSalary.setText(String.format("%.2f", seller.getBaseSalary()));
		if (seller.getDepartment() == null) {
			comboBoxDepartment.getSelectionModel().selectFirst();
		} else {			
			comboBoxDepartment.setValue(seller.getDepartment());
		}
	}

	private void setErrorMessages(Map<String, String> errors) {
		Set<String> fields = errors.keySet();
		if (fields.contains("name")) {
			labelErrorName.setText(errors.get("name"));
		}
	}

	private void initializeComboBoxDepartment() {
		Callback<ListView<Department>, ListCell<Department>> factory = lv -> new ListCell<Department>() {
			@Override
			protected void updateItem(Department item, boolean empty) {
				super.updateItem(item, empty);
				setText(empty ? "" : item.getName());
			}
		};
		comboBoxDepartment.setCellFactory(factory);
		comboBoxDepartment.setButtonCell(factory.call(null));
	}
}
