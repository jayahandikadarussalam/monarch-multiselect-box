# Monarch Multiselect Box

Plugin form Joget DX 8 (Java 11+) untuk memilih beberapa nilai dengan pencarian.
Class plugin tetap `id.co.itasoft.monarch.plugin.MonarchMultiSelectBox`, sehingga referensi field lama tetap menggunakan class yang sama. Nama di palette sekarang **Monarch Multiselect Box** (sebelumnya Shadow Multiselect Box).

## Build dan pemasangan

```sh
mvn package
```

Dependency `org.joget:wflow-core:8.0-SNAPSHOT` harus tersedia di repository Maven lokal atau repository Joget yang dikonfigurasi pada lingkungan build.

Unggah `target/monarch-multiselect-box-8.0.2.jar` melalui pengelolaan plugin Joget. Buka Form Builder, pilih **Monarch → Monarch Multiselect Box**, lalu atur ID, label, dan options atau options binder. Field selalu mendukung multiple selection; nilai tetap disimpan mengikuti format Joget dengan pemisah titik koma.

## Pengaturan UI

- **Lebar field**: default `40%`, sama dengan Multi Select Box bawaan; pada layar kecil menggunakan `100%`.
- **Placeholder**: teks sebelum ada pilihan (default: `Pilih opsi...`).
- **Teks bantuan**: petunjuk singkat di bawah field.
- **Read only**: pilihan ditampilkan dalam kontrol nonaktif dengan hidden input untuk mempertahankan nilai.
- **Display as label when read only**: tampilkan label nilai mengikuti tampilan read-only Joget.
- **Control field**: gunakan grouping atau AJAX options binder sesuai konfigurasi Joget.

Komponen menggunakan **Chosen bawaan Joget** (`/js/chosen/chosen.jquery.js` dan `chosen.css`) agar chip pilihan, ukuran, warna, serta layout mengikuti tema form yang aktif. Plugin tidak memuat Select2 atau CDN. Script inisialisasi memakai ID elemen stabil sehingga dapat dijalankan pada form yang dirender melalui AJAX.

URL aset plugin berbentuk `/plugin/id.co.itasoft.monarch.plugin.MonarchMultiSelectBox/js/...` dan `/css/...`. Joget menambahkan direktori `/resources` pada sisi server; URL browser tidak boleh menambahkan `/resources` lagi. Jika pemuatan aset gagal, select HTML ditampilkan sebagai fallback dan kesalahan dicatat pada console.

## Pembaruan dari 8.0.1

Versi 8.0.2 memperbaiki URL aset yang sebelumnya menghasilkan `/resources/resources/...`, menghapus ketergantungan pada `document.currentScript`, dan mengganti UI Select2 kustom dengan Chosen yang digunakan Multi Select Box Joget. Unggah JAR 8.0.2 untuk mengganti versi lama, lalu muat ulang halaman form.
## Verifikasi

`mvn test` menguji render FreeMarker, escaping label/value, nilai terpilih, hidden input read-only, opsi kosong, dan pesan validasi. Pengujian loader JavaScript: `node --test src/test/js/monarch-loader.test.js` (render melalui AJAX, pemuatan aset bersama untuk beberapa field, pemakaian Chosen yang sudah tersedia, dan fallback/retry). Pemeriksaan sintaks JavaScript: `node --check src/main/resources/resources/js/monarch-multiselect.js`.

Setelah pemasangan, periksa pada tema Joget yang digunakan: beberapa field dalam satu form, pencarian dan penghapusan pilihan, penyimpanan pilihan kosong, read-only, control field (grouping/AJAX), validasi wajib, dan lebar layar ponsel. Pengujian integrasi pada server Joget aktif belum dilakukan di workspace ini.
