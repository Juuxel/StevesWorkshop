package vswe.production.page;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import vswe.production.block.ModBlocks;
import vswe.production.gui.component.ArrowScroll;
import vswe.production.gui.component.CheckBox;
import vswe.production.gui.GuiBase;
import vswe.production.gui.GuiTable;
import vswe.production.gui.menu.GuiMenuItem;
import vswe.production.page.setting.Direction;
import vswe.production.page.setting.ItemSetting;
import vswe.production.page.setting.Setting;
import vswe.production.page.setting.SettingCoal;
import vswe.production.page.setting.SettingNormal;
import vswe.production.page.setting.Side;
import vswe.production.page.setting.Transfer;
import vswe.production.tileentity.TileEntityTable;
import vswe.production.network.data.DataSide;
import vswe.production.network.data.DataType;

import java.util.ArrayList;
import java.util.List;


public class PageTransfer extends Page {
    private List<Setting> settings;
    private Setting selectedSetting;
    private Side selectedSide;
    private List<CheckBox> checkBoxes;
    private List<ArrowScroll> arrows;
    private boolean selectMode;
    private Transfer selectedTransfer;

    public PageTransfer(TileEntityTable table, String name) {
        super(table, name);

        settings = new ArrayList<Setting>();
        for (int i = 0; i < 4; i++) {
            int x = SETTING_X + (i % 2) * SETTING_OFFSET;
            int y = SETTING_Y + (i / 2) * SETTING_OFFSET;
            settings.add(new SettingNormal(table, i, x, y));
        }
        settings.add(new SettingCoal(table, 4, SETTING_X + 2 * SETTING_OFFSET, SETTING_Y + SETTING_OFFSET / 2));


        for (Setting setting : settings) {
            for (Direction direction : Direction.values()) {
                setting.getSides().add(new Side(setting, direction, SIDE_X + direction.getInterfaceX() * SIDE_OFFSET, SIDE_Y + direction.getInterfaceY() * SIDE_OFFSET));
            }
        }

        checkBoxes = new ArrayList<CheckBox>();
        checkBoxes.add(new CheckBox("Select mode", 165, 20) {
            @Override
            public void setValue(boolean value) {
                selectMode = value;
            }

            @Override
            public boolean getValue() {
                return selectMode;
            }

            @Override
            public void onUpdate() {
                if (!getValue()) {
                    selectedSide = null;
                    selectedTransfer = null;
                }
            }

            @Override
            public boolean isVisible() {
                return selectedSetting != null; //TODO should also only be visible if select mode can do something (i.e. if there is a filter or an auto transfer upgrade)
            }
        });

        checkBoxes.add(new CheckBox("Enabled", 170, 58) {
            @Override
            public void setValue(boolean value) {
                selectedTransfer.setEnabled(value);
                PageTransfer.this.table.updateServer(DataType.SIDE_ENABLED, DataSide.getId(selectedSetting, selectedSide, selectedTransfer));
                PageTransfer.this.table.onSideChange();
            }

            @Override
            public boolean getValue() {
                return selectedTransfer.isEnabled();
            }

            @Override
            public boolean isVisible() {
                return selectedTransfer != null;
            }
        });

        checkBoxes.add(new CheckBox("Auto transfer", 170, 68) {
            @Override
            public void setValue(boolean value) {
                selectedTransfer.setAuto(value);
                PageTransfer.this.table.updateServer(DataType.SIDE_AUTO, DataSide.getId(selectedSetting, selectedSide, selectedTransfer));
            }

            @Override
            public boolean getValue() {
                return selectedTransfer.isAuto();
            }

            @Override
            public boolean isVisible() {
                return selectedTransfer != null;
            }
        });

        arrows = new ArrayList<ArrowScroll>();
        arrows.add(new ArrowScroll(165, 40, 50, 2) {
            @Override
            public String getText() {
                return selectedTransfer.isInput() ? "Input" : "Output";
            }

            @Override
            public void setId(int id) {
                selectedTransfer = id == 0 ? selectedSide.getInput() : selectedSide.getOutput();
            }

            @Override
            public int getId() {
                return selectedTransfer.isInput() ? 0 : 1;
            }

            @Override
            public boolean isVisible() {
                return selectedTransfer != null;
            }
        });

        arrows.add(new ArrowScroll(10, 112, 80, 2) {
            @Override
            public String getText() {
                return selectedTransfer.hasWhiteList() ? "Use white list" : "Use black list";
            }

            @Override
            public void setId(int id) {
                selectedTransfer.setUseWhiteList(id == 0);
            }

            @Override
            public int getId() {
                return selectedTransfer.hasWhiteList() ? 0 : 1;
            }

            @Override
            public boolean isVisible() {
                return selectedTransfer != null;
            }

            @Override
            public void onUpdate() {
                PageTransfer.this.table.updateServer(DataType.SIDE_WHITE_LIST, DataSide.getId(selectedSetting, selectedSide, selectedTransfer));
            }
        });
    }


    private static final int SIDE_X = 75;
    private static final int SIDE_Y = 15;
    private static final int SIDE_OFFSET = 20;
    private static final int SIDE_SIZE = 18;
    private static final int SIDE_SRC_X = 0;
    private static final int SIDE_SRC_Y = 166;
    private static final int SIDE_ITEM_OFFSET = 1;

    private static final int SETTING_X = 5;
    private static final int SETTING_Y = 25;
    private static final int SETTING_OFFSET = 20;
    private static final int SETTING_SIZE = 18;
    private static final int SETTING_SRC_X = 0;
    private static final int SETTING_SRC_Y = 112;
    private static final int SETTING_ITEM_OFFSET = 1;

    private static final int ITEM_X = 10;
    private static final int ITEM_Y = 125;
    private static final int ITEM_OFFSET = 20;
    private static final int ITEM_SIZE = 18;
    private static final int ITEM_SRC_X = 68;
    private static final int ITEM_SRC_Y = 62;
    private static final int ITEM_ITEM_OFFSET = 1;

    @Override
    public int createSlots(int id) {
        return id;
    }

    @Override
    public void draw(GuiBase gui, int mX, int mY) {
        super.draw(gui, mX, mY);

        for (Setting setting : settings) {
            gui.prepare();
            boolean isValid = setting.isValid();
            boolean isSelected = setting.equals(selectedSetting);

            if (isSelected && !isValid) {
                selectedTransfer = null;
                selectedSide = null;
                selectedSetting = null;
            }

            int textureIndexX = isValid && gui.inBounds(setting.getX(), setting.getY(), SETTING_SIZE, SETTING_SIZE, mX, mY) ? 1 : 0;
            int textureIndexY = isValid ? isSelected ? 1 : 0 : 2;


            gui.drawRect(setting.getX(), setting.getY(), SETTING_SRC_X + textureIndexX * SETTING_SIZE, SETTING_SRC_Y + textureIndexY * SETTING_SIZE, SETTING_SIZE, SETTING_SIZE);
            gui.drawItem(setting.getItem(), setting.getX() + SETTING_ITEM_OFFSET, setting.getY() + SETTING_ITEM_OFFSET);
        }

        if (selectedSetting != null) {
            for (Side side : selectedSetting.getSides()) {
                gui.prepare();
                int textureIndexX = side.equals(selectedSide) ? 2 : gui.inBounds(side.getX(), side.getY(), SIDE_SIZE, SIDE_SIZE, mX, mY) ? 1 : 0;
                boolean output = side.isOutputEnabled();
                boolean input = side.isInputEnabled();
                int textureIndexY = output && input ? 3 : output ? 2 : input ? 1 : 0;


                gui.drawRect(side.getX(), side.getY(), SIDE_SRC_X + textureIndexX * SIDE_SIZE, SIDE_SRC_Y + textureIndexY * SIDE_SIZE, SIDE_SIZE, SIDE_SIZE);
                gui.drawBlockIcon(ModBlocks.table.getIcon(side.getDirection().ordinal(), 0), side.getX() + SIDE_ITEM_OFFSET, side.getY() + SIDE_ITEM_OFFSET);
            }

            if (selectedTransfer != null) {
                gui.drawString("Filter", 8, 100, 0x404040);

                for (int i = 0; i < ItemSetting.ITEM_COUNT; i++) {
                    gui.prepare();
                    ItemSetting setting = selectedTransfer.getItem(i);
                    ItemStack item = setting != null ? setting.getItem() : null;
                    int x = ITEM_X + i * ITEM_OFFSET;

                    int textureIndexX = gui.inBounds(x, ITEM_Y, ITEM_SIZE, ITEM_SIZE, mX, mY) ? 1 : 0;
                    int textureIndexY = item != null ? 1 : 0;


                    gui.drawRect(x, ITEM_Y, ITEM_SRC_X + textureIndexX * ITEM_SIZE, ITEM_SRC_Y + textureIndexY * ITEM_SIZE, ITEM_SIZE, ITEM_SIZE);
                    gui.drawItem(item, x + ITEM_ITEM_OFFSET, ITEM_Y + ITEM_ITEM_OFFSET);
                }
            }
        }

        for (CheckBox checkBox : checkBoxes) {
            checkBox.draw(gui, mX, mY);
        }
        for (ArrowScroll arrow : arrows) {
            arrow.draw(gui, mX, mY);
        }
    }

    @Override
    public void onClick(GuiBase gui, int mX, int mY, int button) {
        for (Setting setting : settings) {
            if (gui.inBounds(setting.getX(), setting.getY(), SETTING_SIZE, SETTING_SIZE, mX, mY)) {
                if (setting.isValid()) {
                    if (setting.equals(selectedSetting)) {
                        selectedSetting = null;
                        selectedSide = null;
                        selectedTransfer = null;
                    }else{
                        if (selectedSide != null) {
                            Side side = setting.getSides().get(selectedSide.getDirection().ordinal());
                            selectedTransfer = selectedTransfer.isInput() ? side.getInput() : side.getOutput();
                            selectedSide = side;
                        }
                        selectedSetting = setting;
                    }
                }

                break;
            }
        }

        if (selectedSetting != null) {
            for (Side side : selectedSetting.getSides()) {
                if (gui.inBounds(side.getX(), side.getY(), SIDE_SIZE, SIDE_SIZE, mX, mY)) {
                    if (selectMode) {
                        if (side.equals(selectedSide)) {
                            selectedSide = null;
                            selectedTransfer = null;
                        }else{
                            if (selectedTransfer == null) {
                                selectedTransfer = side.getInput();
                            }else{
                                selectedTransfer = selectedTransfer.isInput() ? side.getInput() : side.getOutput();
                            }
                            selectedSide = side;
                        }

                    }else{
                        boolean input = side.isInputEnabled();
                        boolean output = side.isOutputEnabled();

                        int id = (output ? 2 : 0) + (input ? 1 : 0);
                        id += button == 0 ? 1 : -1;
                        if (id < 0) {
                            id += 4;
                        }else{
                            id %= 4;
                        }

                        boolean newInput = (id & 1) != 0;
                        boolean newOutput = (id & 2) != 0;
                        if (newInput != input) {
                            side.setInputEnabled(newInput);
                            table.updateServer(DataType.SIDE_ENABLED, DataSide.getId(selectedSetting, side, side.getInput()));
                        }
                        if (newOutput != output) {
                            side.setOutputEnabled(newOutput);
                            table.updateServer(DataType.SIDE_ENABLED, DataSide.getId(selectedSetting, side, side.getOutput()));
                        }

                        table.onSideChange();
                    }
                    break;
                }
            }

            if(selectedTransfer != null) {
                for (int i = 0; i < ItemSetting.ITEM_COUNT; i++) {
                    if (gui.inBounds(ITEM_X + i * ITEM_OFFSET, ITEM_Y, ITEM_SIZE, ITEM_SIZE, mX, mY)) {
                        EntityPlayer player = gui.mc.thePlayer;
                        ItemStack itemStack = player.inventory.getItemStack();
                        if (itemStack == null) {
                            table.setMenu(new GuiMenuItem(table, selectedTransfer.getItem(i)));
                        }else{
                            itemStack = itemStack.copy();
                            itemStack.stackSize = 1;
                            selectedTransfer.getItem(i).setItem(itemStack);
                            table.updateServer(DataType.SIDE_FILTER, DataSide.Filter.getId(selectedSetting, selectedSide, selectedTransfer, selectedTransfer.getItem(i)));
                        }

                        break;
                    }
                }
            }
        }

        for (CheckBox checkBox : checkBoxes) {
            checkBox.onClick(gui, mX, mY);
        }
        for (ArrowScroll arrow : arrows) {
            arrow.onClick(gui, mX, mY);
        }


    }

    @Override
    public void onRelease(GuiTable gui, int mX, int mY, int button) {
        for (ArrowScroll arrow : arrows) {
            arrow.onRelease();
        }
    }

    public List<Setting> getSettings() {
        return settings;
    }
}
