package fingerscale.ui;

import fingerscale.record.Measurement;
import fingerscale.record.RecordStore;
import fingerscale.unit.Unit;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.util.List;

/** 저장한 측정 기록을 표로 보여준다. */
public class RecordsDialog extends JDialog {
    private final RecordStore store;
    private final Unit unit;
    private final DefaultTableModel model = new DefaultTableModel(new Object[]{"이름", "무게", "시간"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    public RecordsDialog(JFrame owner, RecordStore store, Unit unit) {
        super(owner, "측정 기록", true);
        this.store = store;
        this.unit = unit;

        JTable table = new JTable(model);
        table.setRowHeight(24);

        JButton clearButton = new JButton("전체 삭제");
        clearButton.addActionListener(e -> clearAll());
        JButton closeButton = new JButton("닫기");
        closeButton.addActionListener(e -> dispose());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(clearButton);
        buttons.add(closeButton);

        setLayout(new BorderLayout());
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
        setSize(420, 320);
        setLocationRelativeTo(owner);
        reload();
    }

    private void reload() {
        model.setRowCount(0);
        try {
            List<Measurement> list = store.loadAll();
            for (int i = list.size() - 1; i >= 0; i--) { // 최근 기록이 위로
                Measurement m = list.get(i);
                model.addRow(new Object[]{m.getName(), unit.format(m.getGrams()), m.getTimeText()});
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "기록을 불러오지 못했습니다: " + e.getMessage());
        }
    }

    private void clearAll() {
        int answer = JOptionPane.showConfirmDialog(this, "기록을 모두 지울까요?", "전체 삭제", JOptionPane.YES_NO_OPTION);
        if (answer != JOptionPane.YES_OPTION) return;
        try {
            store.clear();
            reload();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "삭제하지 못했습니다: " + e.getMessage());
        }
    }
}
