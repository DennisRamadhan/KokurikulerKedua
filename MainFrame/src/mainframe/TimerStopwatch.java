import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

// KELAS UTAMA - JFrame sebagai window aplikasi
public class TimerStopwatch extends JFrame {

    //DEKLARASI KOMPONEN UI
    //Label buat nampilin data real-time
    private JLabel lblTime, lblScore, lblStatus, lblPutaran, lblModeInfo;

    //Tombol-tombol kontrol
    private JButton btnStart, btnPause, btnLap, btnSave, btnReset;

    //ComboBox buat pilihan dropdown (mirip select box di web)
    private JComboBox<String> cbGender, cbJarak, cbMode;

    //TextField buat input teks biasa
    private JTextField txtNamaUser, txtTimerSet;

    //TextArea buat history data (bisa banyak baris)
    private JTextArea txtHistory;

    //Timer dari Swing, bukan java.util.Timer (beda ya ges)
    private Timer timer;

    //VARIABEL LOGIKA
    private int centis = 0;       //nyimpen waktu dalam satuan 100ms (centisecond)
    private int putaran = 0;      //jumlah lap yang ditempuh
    private int score = 0;        //nilai akhir (100/90/80/70/60)
    private boolean isRunning = false;   //status timer lagi jalan apa kaga
    private boolean isPaused = false;    //status lagi pause apa kaga
    private boolean isStopwatchMode = true;  //true = stopwatch, false = timer
    private int timerSetCentis = 6000;  //waktu awal buat mode timer (default 60 detik = 6000 centis)

    //KONSTRUKTOR - tempat semua UI dibikin dan dirakit
    public TimerStopwatch() {
        //Setting dasar window
        setTitle("Shuttle Run");
        setSize(1100, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);  //tengahin window di layar
        setLayout(new BorderLayout());

        //PANEL KIRI - TABEL NILAI (Sidebar kiri kayak history)
        JPanel pnlNilai = createTabelNilaiPanel();
        pnlNilai.setPreferredSize(new Dimension(320, 0));  //lebar 320px, tinggi auto
        add(pnlNilai, BorderLayout.WEST);  // WEST = kiri

        //PANEL TENGAH - INPUT + DISPLAY (Ini inti aplikasinya)
        JPanel pnlCenter = new JPanel(new BorderLayout());

        //Panel Input (atas)
        //GridLayout(5,1) = 5 baris, 1 kolom, jarak antar komponen 5px
        JPanel pnlInput = new JPanel(new GridLayout(5, 1, 5, 5));
        pnlInput.setBackground(new Color(44, 62, 80));  //warna gelap estetik
        pnlInput.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        //Inisialisasi komponen input
        txtNamaUser = new JTextField(15);
        cbGender = new JComboBox<>(new String[]{"Laki-laki", "Perempuan"});
        cbJarak = new JComboBox<>(new String[]{"10", "20"});
        cbMode = new JComboBox<>(new String[]{"Stopwatch (Ukur Waktu)", "Timer (Hitung Mundur)"});
        txtTimerSet = new JTextField("60", 5);
        txtTimerSet.setEnabled(false);  //default mati, nyala kalo mode timer

        //Tambahin ke panel input pake method helper biar rapi
        pnlInput.add(createInputRow("Nama User:", txtNamaUser));
        pnlInput.add(createInputRow("Gender:", cbGender));
        pnlInput.add(createInputRow("Jarak Cone (m):", cbJarak));
        pnlInput.add(createInputRow("Mode:", cbMode));
        pnlInput.add(createInputRow("Timer (detik):", txtTimerSet));

        //Panel Display (tengah)
        //GridLayout(5,1) = 5 baris data yang ditampilin
        JPanel pnlDisplay = new JPanel(new GridLayout(5, 1));
        pnlDisplay.setBackground(Color.WHITE);

        lblStatus = new JLabel("READY", SwingConstants.CENTER);
        lblModeInfo = new JLabel("Mode: Stopwatch", SwingConstants.CENTER);
        lblModeInfo.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        lblModeInfo.setForeground(new Color(52, 152, 219));  //warna biru

        //Label waktu pake font monospace biar angkanya ga lari-lari
        lblTime = new JLabel("00:00", SwingConstants.CENTER);
        lblTime.setFont(new Font("Monospaced", Font.BOLD, 80));

        lblPutaran = new JLabel("Putaran: 0", SwingConstants.CENTER);
        lblScore = new JLabel("Skor: -", SwingConstants.CENTER);
        lblScore.setFont(new Font("Segoe UI", Font.BOLD, 25));

        //Urutan nampilin dari atas ke bawah
        pnlDisplay.add(lblStatus);
        pnlDisplay.add(lblModeInfo);
        pnlDisplay.add(lblTime);
        pnlDisplay.add(lblPutaran);
        pnlDisplay.add(lblScore);

        //Rakit panel tengah
        pnlCenter.add(pnlInput, BorderLayout.NORTH);
        pnlCenter.add(pnlDisplay, BorderLayout.CENTER);

        //PANEL KANAN - HISTORY DATA (Sidebar kanan)
        JPanel pnlHistory = new JPanel(new BorderLayout());
        pnlHistory.setBorder(BorderFactory.createTitledBorder("HISTORY DATA"));
        pnlHistory.setPreferredSize(new Dimension(320, 0));

        txtHistory = new JTextArea();
        txtHistory.setEditable(false);  //biar ga bisa diedit manual
        txtHistory.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollHistory = new JScrollPane(txtHistory);
        pnlHistory.add(scrollHistory, BorderLayout.CENTER);

        //PANEL BAWAH - TOMBOL-TOMBOL (South)
        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));

        btnStart = new JButton("START");
        btnPause = new JButton("PAUSE");
        btnLap = new JButton("LAP +1");
        btnSave = new JButton("SAVE");
        btnReset = new JButton("RESET");

        //Styling tombol pake warna-warna aesthetic
        styleButton(btnStart, new Color(46, 204, 113));   //Hijau
        styleButton(btnPause, new Color(231, 76, 60));    //Merah
        styleButton(btnLap, new Color(52, 152, 219));     //Biru
        styleButton(btnSave, new Color(155, 89, 182));    //Ungu
        styleButton(btnReset, new Color(241, 196, 15));   //Kuning

        pnlButtons.add(btnStart);
        pnlButtons.add(btnPause);
        pnlButtons.add(btnLap);
        pnlButtons.add(btnSave);
        pnlButtons.add(btnReset);

        //GABUNG SEMUA PANEL KE FRAME UTAMA
        add(pnlCenter, BorderLayout.CENTER);   //Tengah
        add(pnlHistory, BorderLayout.EAST);    //Kanan
        add(pnlButtons, BorderLayout.SOUTH);   //Bawah

        //TIMER LOGIC - 100ms precision biar akurat untuk shuttle run
        timer = new Timer(100, e -> {
            if (isStopwatchMode) {
                centis++;  //nambah per 100ms
            } else {
                if (centis > 0) {
                    centis--;  //kurang per 100ms
                } else {
                    handleTimerFinish();  //waktu abis
                    return;
                }
            }
            updateTimeDisplay();  //refresh tampilan waktu
        });

        //EVENT HANDLERS - Yang terjadi kalo tombol ditekan

        //ComboBox Mode berubah
        cbMode.addActionListener(e -> {
            isStopwatchMode = cbMode.getSelectedIndex() == 0;
            txtTimerSet.setEnabled(!isStopwatchMode);  //nyala/matiin field timer
            lblModeInfo.setText("Mode: " + (isStopwatchMode ? "Stopwatch" : "Timer"));
            resetAll();  //reset semua kalo ganti mode
        });

        //Tombol START
        btnStart.addActionListener(e -> {
            //Validasi nama ga boleh kosong (biar bisa masuk ke history)
            if (txtNamaUser.getText().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nama harus diisi!");
                return;
            }
            //Kalo baru mulai (bukan resume dari pause)
            if (!isRunning && !isPaused) {
                if (isStopwatchMode) {
                    centis = 0;  // stopwatch mulai dari 0
                } else {
                    //Ambil waktu timer dari text field (input dalam detik)
                    try {
                        int inputDetik = Integer.parseInt(txtTimerSet.getText());
                        if (inputDetik <= 0) throw new NumberFormatException();
                        timerSetCentis = inputDetik * 10;  //konversi detik ke centisecond
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(this, "Masukkan waktu timer yang valid (detik > 0)!");
                        return;
                    }
                    centis = timerSetCentis;  //timer mulai dari angka yang diinput
                }
                //Reset data lama
                putaran = 0;
                score = 0;
                lblPutaran.setText("LAP : 0");
                lblScore.setText("Skor: -");
                updateTimeDisplay();
            }
            timer.start();  //nyalain timer
            isRunning = true;
            isPaused = false;
            lblStatus.setText(isStopwatchMode ? "RUNNING..." : "TIMER RUNNING...");
            lockUI(false);  //kunci input biar ga diganti pas jalan
        });

        //Tombol PAUSE
        btnPause.addActionListener(e -> {
            if (isRunning) {
                timer.stop();  //berhentiin timer
                isRunning = false;
                isPaused = true;
                lblStatus.setText("PAUSED");
                calculateScore();  //hitung skor berdasarkan waktu & putaran
            }
        });

        //Tombol LAP +1
        btnLap.addActionListener(e -> {
            if (isRunning) {
                putaran++;  //nambah 1 putaran
                lblPutaran.setText("LAP : " + putaran);
                calculateScore();  //update skor real-time
            }
        });

        //Tombol SAVE
        btnSave.addActionListener(e -> {
            calculateScore();
            simpanKeHistory();  //simpen ke panel history
            JOptionPane.showMessageDialog(this, "Data berhasil disimpan ke riwayat!",
                    "Simpan Riwayat", JOptionPane.INFORMATION_MESSAGE);
        });

        //Tombol RESET
        btnReset.addActionListener(e -> {
            resetAll();  // balikin semua ke awal
        });
    }

    // METHOD RESET ALL - Balikin ke kondisi awal
    private void resetAll() {
        timer.stop();
        isRunning = false;
        isPaused = false;
        putaran = 0;
        score = 0;
        lblPutaran.setText("LAP : 0");
        lblScore.setText("Skor: -");
        lblStatus.setText("READY");
        btnLap.setEnabled(true);
        lockUI(true);  // buka kunci input

        if (isStopwatchMode) {
            centis = 0;
        } else {
            try {
                int inputDetik = Integer.parseInt(txtTimerSet.getText());
                timerSetCentis = inputDetik * 10;
                centis = timerSetCentis;
            } catch (NumberFormatException ex) {
                centis = 6000;  //default 60 detik kalo input salah
            }
        }
        updateTimeDisplay();
    }

    //METHOD TIMER HABIS - Kalo mode timer & waktu abis
    private void handleTimerFinish() {
        timer.stop();
        isRunning = false;
        btnLap.setEnabled(false);  //matiin tombol lap
        lblStatus.setText("Yah waktunya habis");
        calculateScore();
        JOptionPane.showMessageDialog(this, "Waktu Habis!\nPutaran: " + putaran + "\nSkor Akhir: " + score);
    }

    //PANEL TABEL NILAI - Sidebar kiri, parameter shuttle run amatir
    private JPanel createTabelNilaiPanel() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(44, 62, 80), 2),
            "TABEL NILAI SHUTTLE RUN",
            TitledBorder.CENTER,
            TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 13),
            new Color(44, 62, 80)
        ));
        pnl.setBackground(Color.WHITE);

        //TABEL 10 METER - 4x10m, wajib 4 putaran
        String[] kolom10 = {"Skor", "Laki-laki", "Perempuan"};
        String[][] data10 = {
            {"100", "< 14 dtk",     "< 16 dtk"},
            {"90",  "16 dtk",  "18 dtk"},
            {"80",  "18 dtk",  "20 dtk"},
            {"70",  "20 dtk",  "22 dtk"},
            {"60",  "> 22 dtk",     "> 24 dtk"}
        };

        JTable table10 = new JTable(data10, kolom10);
        table10.setEnabled(false);
        table10.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        table10.setRowHeight(20);
        table10.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        table10.getTableHeader().setBackground(new Color(46, 204, 113));  //Hijau
        table10.getTableHeader().setForeground(Color.WHITE);
        table10.setGridColor(new Color(200, 200, 200));

        //TABEL 20 METER - 2x20m, wajib 2 putaran
        String[] kolom20 = {"Skor", "Laki-laki", "Perempuan"};
        String[][] data20 = {
            {"100", "< 14 dtk",     "< 16 dtk"},
            {"90",  "16 dtk",  "18 dtk"},
            {"80",  "18 dtk",  "20 dtk"},
            {"70",  "20 dtk",  "22 dtk"},
            {"60",  "> 22 dtk",     "> 24 dtk"}
        };

        JTable table20 = new JTable(data20, kolom20);
        table20.setEnabled(false);
        table20.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        table20.setRowHeight(20);
        table20.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        table20.getTableHeader().setBackground(new Color(52, 152, 219));  //Biru
        table20.getTableHeader().setForeground(Color.WHITE);
        table20.setGridColor(new Color(200, 200, 200));

        //RAKIT DUA TABEL - BoxLayout biar tinggi presisi, ga ada space kosong
        JPanel pnlTables = new JPanel();
        pnlTables.setLayout(new BoxLayout(pnlTables, BoxLayout.Y_AXIS));
        pnlTables.setBackground(Color.WHITE);

        //Label + tabel 10m
        JLabel lbl10 = new JLabel("Jarak 4x10m (40m) — Harus 4 Lap", SwingConstants.CENTER);
        lbl10.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lbl10.setForeground(new Color(46, 204, 113));
        lbl10.setAlignmentX(Component.CENTER_ALIGNMENT);
        lbl10.setBorder(BorderFactory.createEmptyBorder(4, 0, 2, 0));

        int tbl10H = table10.getRowHeight() * data10.length + table10.getTableHeader().getPreferredSize().height;
        JScrollPane scroll10 = new JScrollPane(table10);
        scroll10.setPreferredSize(new Dimension(300, tbl10H));
        scroll10.setMaximumSize(new Dimension(Integer.MAX_VALUE, tbl10H));
        scroll10.setMinimumSize(new Dimension(100, tbl10H));

        //Label + tabel 20m
        JLabel lbl20 = new JLabel("Jarak 2x20m (40m) — Harus 2 Lap", SwingConstants.CENTER);
        lbl20.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lbl20.setForeground(new Color(52, 152, 219));
        lbl20.setAlignmentX(Component.CENTER_ALIGNMENT);
        lbl20.setBorder(BorderFactory.createEmptyBorder(8, 0, 2, 0));

        int tbl20H = table20.getRowHeight() * data20.length + table20.getTableHeader().getPreferredSize().height;
        JScrollPane scroll20 = new JScrollPane(table20);
        scroll20.setPreferredSize(new Dimension(300, tbl20H));
        scroll20.setMaximumSize(new Dimension(Integer.MAX_VALUE, tbl20H));
        scroll20.setMinimumSize(new Dimension(100, tbl20H));

        pnlTables.add(lbl10);
        pnlTables.add(scroll10);
        pnlTables.add(lbl20);
        pnlTables.add(scroll20);

        //Info tambahan di bawah tabel
        JLabel lblInfo = new JLabel(
            "<html><center>Skor berdasarkan waktu tempuh.<br>" +
            "Lap kurang = skor tidak dihitung.</center></html>",
            SwingConstants.CENTER);
        lblInfo.setFont(new Font("Segoe UI", Font.ITALIC, 10));
        lblInfo.setForeground(Color.GRAY);
        lblInfo.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        pnl.add(pnlTables, BorderLayout.NORTH);
        pnl.add(lblInfo, BorderLayout.SOUTH);

        return pnl;
    }

    // METHOD SIMPAN KE HISTORY - Nyimpen data ke panel kanan
    private void simpanKeHistory() {
        String nama = txtNamaUser.getText();
        if (nama.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Nama user belum diisi!",
                    "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        //Ambil semua data yang perlu disimpen
        String gender = cbGender.getSelectedItem().toString();
        int jarak = Integer.parseInt(cbJarak.getSelectedItem().toString());
        String mode = cbMode.getSelectedItem().toString();
        int totalJarak = putaran * jarak;  // jarak total = lap × jarak cone

        //Hitung waktu TERPAKAI, format ke MM:SS
        int elapsedCentis = isStopwatchMode ? centis : (timerSetCentis - centis);
        int totalDetik = elapsedCentis / 10;
        String waktu = String.format("%02d:%02d", totalDetik / 60, totalDetik % 60);

        //Format waktu sekarang
        String timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
        String tanggal = new SimpleDateFormat("dd/MM/yyyy").format(new Date());

        //Rakit string buat dimasukin ke history
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(tanggal).append(" ").append(timestamp).append("] ").append(nama).append("\n");
        sb.append("Mode   : ").append(mode).append("\n");
        sb.append("Gender : ").append(gender).append(" | Jarak: ").append(jarak).append("m\n");
        sb.append("Putaran: ").append(putaran).append(" | Total: ").append(totalJarak).append(" Meter\n");
        sb.append("Waktu  : ").append(waktu).append("\n");
        sb.append("SKOR   : ").append(score == 0 ? "-" : score).append("\n");
        sb.append("----------------------------\n");

        //insert di posisi 0 = paling atas (newest first)
        txtHistory.insert(sb.toString(), 0);
    }

    //METHOD HITUNG SKOR - Wajib putaran sesuai dulu, baru dihitung dari waktu
    private void calculateScore() {
        String gender = cbGender.getSelectedItem().toString();
        int jarak = Integer.parseInt(cbJarak.getSelectedItem().toString());

        //CEK PUTARAN WAJIB - 10m = 4 putaran, 20m = 2 putaran
        //Kalau putaran belum terpenuhi, skor tidak dihitung
        int putaranWajib = (jarak == 10) ? 4 : 2;
        if (putaran < putaranWajib) {
            score = 0;
            lblScore.setText("Skor: - (Harus " + putaranWajib + " Lap)");
            return;
        }

        //Waktu terpakai dalam detik (presisi 1 desimal)
        //- Stopwatch: centis = waktu berjalan
        //- Timer: timerSetCentis - centis = waktu yang sudah terpakai
        double elapsed = isStopwatchMode ? (centis / 10.0) : ((timerSetCentis - centis) / 10.0);

        //LOGIKA NILAI berdasarkan waktu, setelah putaran terpenuhi
        if (jarak == 10) {
            //Parameter 4x10m shuttle run
            if (gender.equals("Laki-laki")) {
                if (elapsed < 14.0)        score = 100;
                else if (elapsed <= 16.0)  score = 90;
                else if (elapsed <= 18.0)  score = 80;
                else if (elapsed <= 20.0)  score = 70;
                else                       score = 60;
            } else {  //Perempuan
                if (elapsed < 16.0)        score = 100;
                else if (elapsed <= 18.0)  score = 90;
                else if (elapsed <= 20.0)  score = 80;
                else if (elapsed <= 22.0)  score = 70;
                else                       score = 60;
            }
        } else {
            //Parameter 2x20m shuttle run
            if (gender.equals("Laki-laki")) {
                if (elapsed < 14.0)        score = 100;
                else if (elapsed <= 16.0)  score = 90;
                else if (elapsed <= 18.0)  score = 80;
                else if (elapsed <= 20.0)  score = 70;
                else                       score = 60;
            } else {  //Perempuan
                if (elapsed < 16.0)        score = 100;
                else if (elapsed <= 18.0)  score = 90;
                else if (elapsed <= 20.0)  score = 80;
                else if (elapsed <= 22.0)  score = 70;
                else                       score = 60;
            }
        }
        lblScore.setText("Skor: " + score);
    }

    //METHOD UPDATE WAKTU - Format MM:SS dari centisecond
    //Contoh: 6050 centis → 605 detik → "10:05"
    private void updateTimeDisplay() {
        int totalDetik = centis / 10;
        lblTime.setText(String.format("%02d:%02d", totalDetik / 60, totalDetik % 60));
    }

    //METHOD KUNCI UI - Biar input ga diganti pas timer jalan
    private void lockUI(boolean enabled) {
        txtNamaUser.setEnabled(enabled);
        cbGender.setEnabled(enabled);
        cbJarak.setEnabled(enabled);
        cbMode.setEnabled(enabled);
        //Timer set cuma nyala kalo mode timer DAN UI unlocked
        txtTimerSet.setEnabled(!isStopwatchMode && enabled);
    }

    //METHOD BIKIN BARIS INPUT - Helper biar rapi
    private JPanel createInputRow(String label, JComponent comp) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p.setOpaque(false);  //transparan biar warna background keliatan
        JLabel l = new JLabel(label);
        l.setForeground(Color.WHITE);  //teks putih biar kontras sama background gelap
        l.setPreferredSize(new Dimension(100, 20));  //lebar label seragam
        p.add(l);
        p.add(comp);
        return p;
    }

    // METHOD STYLING TOMBOL - Biar tombolnya berwarna
    private void styleButton(JButton btn, Color color) {
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setPreferredSize(new Dimension(120, 40));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btn.setFocusPainted(false);  //ilangin border focus yang jelek
    }

    //MAIN METHOD - Entry point aplikasi
    public static void main(String[] args) {
        //invokeLater = pastikan UI dibikin di thread yang bener (EDT)
        SwingUtilities.invokeLater(() -> new TimerStopwatch().setVisible(true));
    }
}