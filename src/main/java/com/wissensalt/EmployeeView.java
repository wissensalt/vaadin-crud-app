package com.wissensalt;

import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility.Border;
import com.vaadin.flow.theme.lumo.LumoUtility.BorderColor;
import com.vaadin.flow.theme.lumo.LumoUtility.BorderRadius;
import com.vaadin.flow.theme.lumo.LumoUtility.Margin;
import com.vaadin.flow.theme.lumo.LumoUtility.Padding.Horizontal;
import com.vaadin.flow.theme.lumo.LumoUtility.Padding.Vertical;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Route("")
public class EmployeeView extends VerticalLayout {

  private final EmployeeRepository employeeRepository;
  private Grid<Employee> employeeGrid;
  private EmailField emailField;
  private TextField tfName;
  private BigDecimalField nfSalary;
  private TextField tfDepartment;
  private Button deleteButton;

  public EmployeeView(EmployeeRepository employeeRepository) {
    this.employeeRepository = employeeRepository;
    setupComponents();
  }

  private void setupComponents() {
    setAlignItems(Alignment.CENTER);
    setSizeFull();
    final H2 formTitle = new H2("Crud Table Employee");
    add(formTitle);
    final FormLayout formEmployee = formEmployee();
    setAlignSelf(Alignment.CENTER, formEmployee);
    add(formEmployee);
    employeeGrid = gridEmployee();
    add(employeeGrid);
  }

  private FormLayout formEmployee() {
    final FormLayout formLayout = new FormLayout();
    formLayout.addClassName(Border.ALL);
    formLayout.addClassName(BorderColor.PRIMARY);
    formLayout.addClassName(BorderRadius.SMALL);
    formLayout.addClassName(Vertical.SMALL);
    formLayout.addClassName(Horizontal.SMALL);
    formLayout.setMaxWidth(30, Unit.PERCENTAGE);
    formLayout.setResponsiveSteps(new ResponsiveStep("0", 1));
    emailField = new EmailField();
    emailField.setLabel("Email");
    tfName = new TextField();
    tfName.setLabel("Name");
    nfSalary = new BigDecimalField();
    nfSalary.setLabel("Salary");
    Div dollarPrefix = new Div();
    dollarPrefix.setText("$");
    nfSalary.setPrefixComponent(dollarPrefix);
    tfDepartment = new TextField();
    tfDepartment.setLabel("Department");
    formLayout.add(emailField);
    formLayout.add(tfName);
    formLayout.add(nfSalary);
    formLayout.add(tfDepartment);

    Button submitButton = new Button("Submit");
    submitButton.addClassName(Margin.MEDIUM);
    submitButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    deleteButton = new Button("Delete");
    deleteButton.addClassName(Margin.MEDIUM);
    deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
    deleteButton.setEnabled(false);
    deleteButton.addClickListener(e -> {
      final Optional<Employee> employeeOptional = employeeRepository.findFirstByEmail(
          emailField.getValue());
      if (employeeOptional.isPresent()) {
        final Employee employee = employeeOptional.get();
        employeeRepository.delete(employee);
        employeeGrid.setItems(employeeRepository.findAll());
      } else {
        showNotification("Employee not found", NotificationType.FAILED);
      }
      deleteButton.setEnabled(false);
      clearForm();
    });
    final HorizontalLayout actionLayout = new HorizontalLayout();
    actionLayout.setWidth(30, Unit.PERCENTAGE);
    actionLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);
    actionLayout.add(deleteButton, submitButton);
    formLayout.add(actionLayout);
    final Binder<Employee> employeeBinder = new Binder<>(Employee.class);
    employeeBinder.forField(emailField).bind(Employee::getEmail, Employee::setEmail);
    employeeBinder.forField(tfName).bind(Employee::getName, Employee::setEmail);
    employeeBinder.forField(nfSalary).bind(Employee::getSalary, Employee::setSalary);
    employeeBinder.forField(tfDepartment).bind(Employee::getDepartment, Employee::setDepartment);
    submitButton.addClickListener(e -> {
      final Employee newEmployee = new Employee();
      newEmployee.setName(tfName.getValue());
      newEmployee.setEmail(emailField.getValue());
      newEmployee.setSalary(nfSalary.getValue());
      newEmployee.setDepartment(tfDepartment.getValue());
      employeeBinder.setBean(newEmployee);
      if (employeeBinder.validate().isOk()) {
        final Optional<Employee> optionalEmployee = employeeRepository.findFirstByEmail(
            newEmployee.getEmail());
        if (optionalEmployee.isPresent()) {
          final Employee existingEmployee = optionalEmployee.get();
          newEmployee.setId(existingEmployee.getId());
        }

        employeeRepository.save(newEmployee);
        showNotification("Success Create a New Employee", NotificationType.SUCCESS);
        final List<Employee> employees = employeeRepository.findAll();
        employeeGrid.setItems(employees);
        clearForm();
      } else {
        showNotification("Some fields are not valid", NotificationType.FAILED);
      }
    });

    return formLayout;
  }

  private void clearForm() {
    emailField.setValue("");
    nfSalary.setValue(BigDecimal.ZERO);
    tfDepartment.setValue("");
    tfName.setValue("");
    emailField.focus();
  }

  enum NotificationType {
    SUCCESS, FAILED
  }

  private void showNotification(String message, NotificationType type) {
    Notification notification = Notification.show(message);
    notification.setDuration(5000);
    if (type.equals(NotificationType.SUCCESS)) {
      notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    } else {
      notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

    notification.open();
  }

  private Grid<Employee> gridEmployee() {
    final Grid<Employee> grid = new Grid<>(Employee.class, false);
    grid.addColumn(Employee::getId).setHeader("Id");
    grid.addColumn(Employee::getEmail).setHeader("Email");
    grid.addColumn(Employee::getName).setHeader("Name");
    grid.addColumn(Employee::getSalary).setHeader("Salary");
    grid.addColumn(Employee::getDepartment).setHeader("Department");
    grid.addSelectionListener(selection -> {
      Optional<Employee> optionalEmployee = selection.getFirstSelectedItem();
      if (optionalEmployee.isPresent()) {
        final Employee employee = optionalEmployee.get();
        emailField.setValue(employee.getEmail());
        tfName.setValue(employee.getName());
        nfSalary.setValue(employee.getSalary());
        tfDepartment.setValue(employee.getDepartment());
        deleteButton.setEnabled(true);
      }
    });

    final List<Employee> people = employeeRepository.findAll();
    grid.setItems(people);

    return grid;
  }


}
