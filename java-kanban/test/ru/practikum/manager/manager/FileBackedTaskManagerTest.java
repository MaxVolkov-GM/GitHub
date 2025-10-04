package ru.practikum.manager;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.practikum.model.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest {

    @TempDir
    Path tempDir;

    @Test
    void testSaveAndLoadEmptyManager() throws IOException {
        File file = tempDir.resolve("test.csv").toFile();

        // Создаем пустой менеджер и сохраняем
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        manager.save(); // Явно вызываем save для пустого состояния

        // Загружаем из файла
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

        assertEquals(0, loadedManager.getAllTasks().size());
        assertEquals(0, loadedManager.getAllEpics().size());
        assertEquals(0, loadedManager.getAllSubtasks().size());
    }

    @Test
    void testSaveAndLoadWithTasks() throws IOException {
        File file = tempDir.resolve("test.csv").toFile();

        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        // Создаем задачи
        Task task = new Task("Task 1", "Description 1", Status.NEW);
        int taskId = manager.createTask(task);

        Epic epic = new Epic("Epic 1", "Description epic");
        int epicId = manager.createEpic(epic);

        Subtask subtask = new Subtask("Subtask 1", "Description subtask", Status.IN_PROGRESS, epicId);
        int subtaskId = manager.createSubtask(subtask);

        // Загружаем из файла
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

        // Проверяем загруженные данные
        assertEquals(1, loadedManager.getAllTasks().size());
        assertEquals(1, loadedManager.getAllEpics().size());
        assertEquals(1, loadedManager.getAllSubtasks().size());

        // Проверяем Task - ВСЕ поля
        Task loadedTask = loadedManager.getTaskById(taskId);
        assertNotNull(loadedTask);
        assertEquals(task.getId(), loadedTask.getId());
        assertEquals(task.getName(), loadedTask.getName());
        assertEquals(task.getDescription(), loadedTask.getDescription());
        assertEquals(task.getStatus(), loadedTask.getStatus());
        assertEquals(task.getType(), loadedTask.getType());

        // Проверяем Epic - ВСЕ поля
        Epic loadedEpic = loadedManager.getEpicById(epicId);
        assertNotNull(loadedEpic);
        assertEquals(epic.getId(), loadedEpic.getId());
        assertEquals(epic.getName(), loadedEpic.getName());
        assertEquals(epic.getDescription(), loadedEpic.getDescription());
        assertEquals(epic.getStatus(), loadedEpic.getStatus());
        assertEquals(epic.getType(), loadedEpic.getType());
        assertEquals(1, loadedEpic.getSubtaskIds().size());

        // Проверяем Subtask - ВСЕ поля
        Subtask loadedSubtask = loadedManager.getSubtaskById(subtaskId);
        assertNotNull(loadedSubtask);
        assertEquals(subtask.getId(), loadedSubtask.getId());
        assertEquals(subtask.getName(), loadedSubtask.getName());
        assertEquals(subtask.getDescription(), loadedSubtask.getDescription());
        assertEquals(subtask.getStatus(), loadedSubtask.getStatus());
        assertEquals(subtask.getType(), loadedSubtask.getType());
        assertEquals(subtask.getEpicId(), loadedSubtask.getEpicId());
    }

    @Test
    void testFileContentFormat() throws IOException {
        File file = tempDir.resolve("test.csv").toFile();

        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        Task task = new Task("Test Task", "Test Description", Status.DONE);
        manager.createTask(task);

        // Проверяем содержимое файла
        String content = Files.readString(file.toPath());
        String[] lines = content.split("\n");

        assertEquals("id,type,name,status,description,epic", lines[0]);
        assertTrue(lines[1].startsWith("1,TASK,Test Task,DONE,Test Description,"));
    }

    @Test
    void testEpicSubtaskRelationshipAfterLoad() throws IOException {
        File file = tempDir.resolve("test.csv").toFile();

        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        Epic epic = new Epic("Epic", "Epic description");
        int epicId = manager.createEpic(epic);

        Subtask subtask = new Subtask("Subtask", "Subtask description", Status.NEW, epicId);
        manager.createSubtask(subtask);

        // Загружаем из файла
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

        Epic loadedEpic = loadedManager.getEpicById(epicId);
        assertNotNull(loadedEpic);
        assertEquals(1, loadedEpic.getSubtaskIds().size());

        // Проверяем что подзадача связана с эпиком
        Subtask loadedSubtask = loadedManager.getSubtaskById(2); // ID подзадачи будет 2
        assertNotNull(loadedSubtask);
        assertEquals(epicId, loadedSubtask.getEpicId());
    }
}
