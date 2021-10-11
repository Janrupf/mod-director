package net.jan.moddirector.core.manage.select;

public class SelectableInstallOption {
    private final String description;
    private final String name;

    private boolean selected;

    public SelectableInstallOption(boolean selectedByDefault, String name, String description) {
        this.selected = selectedByDefault;
        this.name = name;
        this.description = description;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
