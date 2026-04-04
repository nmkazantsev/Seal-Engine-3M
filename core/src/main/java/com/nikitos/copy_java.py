import os
import shutil

def copy_java_files(src_dir, dest_dir="temp"):
    # Создаём целевую папку, если её нет (лучше заранее, чтобы исключить её из обхода)
    os.makedirs(dest_dir, exist_ok=True)

    # Преобразуем пути в абсолютные для корректного сравнения
    src_dir_abs = os.path.abspath(src_dir)
    dest_dir_abs = os.path.abspath(dest_dir)

    # Рекурсивный обход
    for root, dirs, files in os.walk(src_dir_abs):
        # Исключаем целевую папку из обхода (если она оказалась внутри src_dir)
        if os.path.abspath(root) == dest_dir_abs:
            continue

        for file in files:
            if file.lower().endswith(".java") and not("bridge" in file.lower()):
                src_path = os.path.join(root, file)
                base_name = file
                dest_path = os.path.join(dest_dir_abs, base_name)

                # Разрешение конфликта имён
                if os.path.exists(dest_path):
                    name, ext = os.path.splitext(base_name)
                    counter = 1
                    while True:
                        new_name = f"{name}_{counter}{ext}"
                        dest_path = os.path.join(dest_dir_abs, new_name)
                        if not os.path.exists(dest_path):
                            break
                        counter += 1
                    print(f"Конфликт имён: {file} -> {new_name}")

                try:
                    shutil.copy2(src_path, dest_path)
                    print(f"Скопирован: {src_path} -> {dest_path}")
                except Exception as e:
                    print(f"Ошибка при копировании {src_path}: {e}")

if __name__ == "__main__":
    start_dir = os.getcwd()
    copy_java_files(start_dir)
    print("Готово!")
