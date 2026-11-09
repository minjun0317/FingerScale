package fingerscale.ui;

import fingerscale.record.Measurement;
import fingerscale.record.RecordStore;
import fingerscale.scale.ScaleController;
import fingerscale.scale.ScaleListener;
import fingerscale.scale.ScaleState;
import fingerscale.settings.Settings;
import fingerscale.source.PressureReading;
import fingerscale.unit.Gram;
import fingerscale.unit.Ounce;
import fingerscale.unit.Unit;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.time.LocalDateTime;

/** 저울 화면. 판단은 ScaleController가 하고, 여기서는 보여주기와 버튼 처리만 한다. */
public class ScaleWindow extends JFrame implements ScaleListener {
    private static final Color BACKGROUND = Color.WHITE;
    private static final Color TEXT = new Color(0x1D1D1F);
    private static final Color MUTED = new Color(0x8E8E93);
    private static final Color GOOD = new Color(0x1E9E4A);
    private static final Color BUSY = new Color(0xD98200);
    private static final Color WARN = new Color(0xD93025);

    private final ScaleController controller;
    private final Settings settings;
    private final RecordStore store;
    private final Unit[] units = {new Gram(), new Ounce()};
    private int unitIndex = 0;

    private final JLabel statusLabel = label("", 17f, Font.PLAIN);
    private final JLabel weightLabel = label("--", 88f, Font.BOLD);
    private final JLabel hintLabel = label("스페이스바: 영점 다시 맞추기", 12f, Font.PLAIN);
    private final JLabel rawLabel = label(" ", 11f, Font.PLAIN);
    private final JLabel messageLabel = label(" ", 13f, Font.PLAIN);
    private final JButton zeroButton = new JButton("영점 맞추기");
    private final JButton unitButton = new JButton();
    private final JTextField nameField = new JTextField(12);
    private final JButton saveButton = new JButton("저장");
    private final Timer messageTimer = new Timer(3000, e -> messageLabel.setText(" "));

    private ScaleState lastState;

    public ScaleWindow(String sourceName, ScaleController controller, Settings settings, RecordStore store) {
        super("손가락저울" + (sourceName.equals("트랙패드") ? "" : " (" + sourceName + ")"));
        this.controller = controller;
        this.settings = settings;
        this.store = store;

        hintLabel.setForeground(MUTED);
        rawLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        rawLabel.setForeground(MUTED);
        rawLabel.setVisible(false);
        messageLabel.setForeground(GOOD);
        messageTimer.setRepeats(false);

        setupButtons();
        setJMenuBar(createMenuBar());
        setContentPane(createContent());
        bindSpaceToZero();

        setMinimumSize(new Dimension(480, 440));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        updateUnitButton();
        onUpdate(ScaleState.NO_TOUCH, 0, new PressureReading(0, 0, 0));
    }

    private static JLabel label(String text, float size, int style) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(l.getFont().deriveFont(style, size));
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        return l;
    }

    private void setupButtons() {
        zeroButton.setFocusable(false);
        zeroButton.addActionListener(e -> zero());

        unitButton.setFocusable(false);
        unitButton.addActionListener(e -> {
            unitIndex = (unitIndex + 1) % units.length;
            updateUnitButton();
            onUpdate(controller.getState(), controller.getWeightGrams(), new PressureReading(0, 0, 0));
        });

        nameField.setToolTipText("비워두면 '측정 1', '측정 2'처럼 저장돼요");
        nameField.addActionListener(e -> save()); // 엔터로 저장
        saveButton.addActionListener(e -> save());
    }

    private JPanel createContent() {
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBackground(BACKGROUND);
        center.setBorder(BorderFactory.createEmptyBorder(28, 24, 12, 24));
        center.setFocusable(true); // 처음 포커스를 이름 칸이 아니라 여기에 → 스페이스바가 바로 먹힌다
        center.add(statusLabel);
        center.add(Box.createVerticalGlue());
        center.add(weightLabel);
        center.add(Box.createVerticalStrut(4));
        center.add(hintLabel);
        center.add(Box.createVerticalGlue());
        center.add(rawLabel);
        center.add(messageLabel);

        JButton recordsButton = new JButton("기록 보기");
        recordsButton.setFocusable(false);
        recordsButton.addActionListener(e -> new RecordsDialog(this, store, currentUnit()).setVisible(true));

        JPanel controls = row(zeroButton, unitButton);
        JPanel saveRow = row(new JLabel("이름"), nameField, saveButton, recordsButton);

        JPanel bottom = new JPanel(new GridLayout(2, 1));
        bottom.setBackground(BACKGROUND);
        bottom.setBorder(BorderFactory.createEmptyBorder(0, 12, 12, 12));
        bottom.add(controls);
        bottom.add(saveRow);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BACKGROUND);
        root.add(center, BorderLayout.CENTER);
        root.add(bottom, BorderLayout.SOUTH);
        SwingUtilities.invokeLater(center::requestFocusInWindow);
        return root;
    }

    private static JPanel row(JComponent... items) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        p.setBackground(BACKGROUND);
        for (JComponent c : items) p.add(c);
        return p;
    }

    private JMenuBar createMenuBar() {
        JMenuItem calibrate = new JMenuItem("보정하기…");
        calibrate.addActionListener(e -> calibrate());

        JCheckBoxMenuItem showRaw = new JCheckBoxMenuItem("압력 값 보기");
        showRaw.addActionListener(e -> rawLabel.setVisible(showRaw.isSelected()));

        JMenu menu = new JMenu("설정");
        menu.add(calibrate);
        menu.add(showRaw);
        JMenuBar bar = new JMenuBar();
        bar.add(menu);
        return bar;
    }

    /** 스페이스바로 영점 (이름 칸에 글자 입력 중일 때는 제외) */
    private void bindSpaceToZero() {
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "zero");
        getRootPane().getActionMap().put("zero", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!nameField.isFocusOwner()) zero();
            }
        });
    }

    private Unit currentUnit() {
        return units[unitIndex];
    }

    private void updateUnitButton() {
        unitButton.setText("단위: " + currentUnit().getSymbol());
    }

    /** ScaleController가 값을 계산할 때마다 불린다. */
    @Override
    public void onUpdate(ScaleState state, double grams, PressureReading reading) {
        if (reading.getFingerCount() > 0 || reading.getPressure() > 0) {
            rawLabel.setText(String.format("손가락 %d개 · 압력 %.0f · 1g당 압력 %.1f",
                    reading.getFingerCount(), reading.getPressure(),
                    1 / controller.getCalculator().getGramsPerUnit()));
        }

        weightLabel.setText(state.showsWeight() ? currentUnit().format(grams) : "--");
        weightLabel.setForeground(state == ScaleState.MEASURING ? MUTED : state.showsWeight() ? TEXT : MUTED);

        if (state != lastState) {
            statusLabel.setText(state.getMessage());
            statusLabel.setForeground(colorOf(state));
            zeroButton.setEnabled(state != ScaleState.NO_TOUCH && state != ScaleState.MULTI_TOUCH);
            hintLabel.setVisible(state.showsWeight() || state == ScaleState.DRIFT);
            lastState = state;
        }
        saveButton.setEnabled(state == ScaleState.STABLE || state == ScaleState.MEASURING);
    }

    private static Color colorOf(ScaleState state) {
        switch (state) {
            case READY:
            case STABLE:
                return GOOD;
            case MEASURING:
                return BUSY;
            case MULTI_TOUCH:
            case DRIFT:
                return WARN;
            default:
                return TEXT;
        }
    }

    private void zero() {
        if (controller.zero()) showMessage("영점을 다시 맞췄어요", GOOD);
    }

    private void save() {
        double grams = controller.getWeightGrams();
        if (grams <= 0) {
            showMessage("물건을 올린 상태에서 저장해주세요", WARN);
            return;
        }
        try {
            String name = nameField.getText().trim();
            if (name.isEmpty()) name = "측정 " + (store.loadAll().size() + 1);
            Measurement m = new Measurement(name, grams, LocalDateTime.now());
            store.save(m);
            nameField.setText("");
            getContentPane().requestFocusInWindow();
            showMessage(m.getName() + " · " + currentUnit().format(m.getGrams()) + " 저장했어요", GOOD);
        } catch (IOException e) {
            showMessage("저장 실패: " + e.getMessage(), WARN);
        }
    }

    private void showMessage(String text, Color color) {
        messageLabel.setForeground(color);
        messageLabel.setText(text);
        messageTimer.restart();
    }

    /** 무게를 아는 물건을 올린 상태에서 그 무게를 입력하면 압력→g 비율을 다시 계산한다. */
    private void calibrate() {
        String input = JOptionPane.showInputDialog(this,
                "1. 손가락만 올려서 '준비 완료'가 뜨게 하고\n"
                        + "2. 무게를 아는 물건을 올린 다음\n"
                        + "3. 그 물건의 실제 무게(g)를 입력하세요\n\n"
                        + "예) 에어팟 프로3 케이스+이어버드 = 55.1",
                "보정하기", JOptionPane.QUESTION_MESSAGE);
        if (input == null) return;
        try {
            double gramsPerUnit = controller.calibrate(Double.parseDouble(input.trim()));
            settings.setGramsPerUnit(gramsPerUnit);
            showMessage(String.format("보정 완료 (1g = 압력 %.1f)", 1 / gramsPerUnit), GOOD);
        } catch (NumberFormatException e) {
            showMessage("무게는 숫자로 입력해주세요", WARN);
        } catch (IllegalStateException | IllegalArgumentException | IOException e) {
            showMessage(e.getMessage(), WARN);
        }
    }
}
