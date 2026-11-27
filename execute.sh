# Biên dịch cả 3 tệp
javac Merge.java DatasetGenerator.java CutoffExperiment.java

# Bước 1: Sinh 20 tệp dữ liệu vào thư mục ./datasets/
java DatasetGenerator

# Bước 2: Chạy đo hiệu năng (lưu ý KHÔNG dùng cờ -ea khi bấm giờ)
java CutoffExperiment