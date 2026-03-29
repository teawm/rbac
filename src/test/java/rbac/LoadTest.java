package rbac;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

public class LoadTest {

    private static final int NUM_THREADS = 5;
    private static final int OPERATIONS_PER_THREAD = 50;
    private static final Random RANDOM = new Random();

    @BeforeEach
    void clearRoleNames() {
        Role.clearExistingNamesForTesting();
    }

    @Test
    void testConcurrentUserOperations() throws InterruptedException {
        RBACSystem system = new RBACSystem();
        UserManager userManager = system.getUserManager();

        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
        CountDownLatch latch = new CountDownLatch(NUM_THREADS);

        for (int i = 0; i < NUM_THREADS; i++) {
            final int threadId = i;
            executor.execute(() -> {
                try {
                    for (int j = 0; j < OPERATIONS_PER_THREAD; j++) {
                        String username = "user_" + threadId + "_" + j;
                        try {
                            User user = User.validate(username, "Test User " + j, username + "@test.com");
                            userManager.add(user);
                        } catch (IllegalArgumentException e) {
                            if (!e.getMessage().contains("уже существует")) {
                                throw e;
                            }
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        int totalUsers = userManager.count();
        assertTrue(totalUsers > 0, "Должны быть созданы пользователи");
        assertTrue(totalUsers <= NUM_THREADS * OPERATIONS_PER_THREAD,
                "Количество пользователей не должно превышать ожидаемое");

        long uniqueUsernames = userManager.findAll().stream()
                .map(User::username)
                .distinct()
                .count();

        assertEquals(totalUsers, uniqueUsernames,
                "Все имена пользователей должны быть уникальными");
    }

    @Test
    void testConcurrentRoleOperations() throws InterruptedException {
        RBACSystem system = new RBACSystem();
        RoleManager roleManager = system.getRoleManager();

        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
        CountDownLatch latch = new CountDownLatch(NUM_THREADS);

        for (int i = 0; i < NUM_THREADS; i++) {
            final int threadId = i;
            executor.execute(() -> {
                try {
                    for (int j = 0; j < OPERATIONS_PER_THREAD; j++) {
                        String roleName = "Role_" + threadId + "_" + j;
                        try {
                            Role role = new Role(roleName, "Test role " + j);
                            role.addPermission(new Permission("READ", "test", "Test permission"));
                            roleManager.add(role);
                        } catch (IllegalArgumentException e) {
                            if (!e.getMessage().contains("уже существует")) {
                                throw e;
                            }
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        int totalRoles = roleManager.count();
        assertTrue(totalRoles > 0, "Должны быть созданы роли");
        assertTrue(totalRoles <= NUM_THREADS * OPERATIONS_PER_THREAD,
                "Количество ролей не должно превышать ожидаемое");

        long uniqueRoleNames = roleManager.findAll().stream()
                .map(Role::name)
                .distinct()
                .count();

        assertEquals(totalRoles, uniqueRoleNames,
                "Все имена ролей должны быть уникальными");
    }

    @Test
    void testConcurrentAssignmentOperations() throws InterruptedException {
        RBACSystem system = new RBACSystem();
        UserManager userManager = system.getUserManager();
        RoleManager roleManager = system.getRoleManager();
        AssignmentManager assignmentManager = system.getAssignmentManager();

        for (int i = 0; i < 10; i++) {
            userManager.add(User.validate("user_" + i, "User " + i, "user" + i + "@test.com"));
            Role role = new Role("Role_" + i, "Role " + i);
            role.addPermission(new Permission("READ", "resource_" + i, "Test"));
            roleManager.add(role);
        }

        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
        CountDownLatch latch = new CountDownLatch(NUM_THREADS);

        for (int i = 0; i < NUM_THREADS; i++) {
            final int threadId = i;
            executor.execute(() -> {
                try {
                    for (int j = 0; j < OPERATIONS_PER_THREAD; j++) {
                        try {
                            User user = userManager.findByUsername("user_" + (j % 10)).orElseThrow();
                            Role role = roleManager.findByName("Role_" + (j % 10)).orElseThrow();
                            AssignmentMetadata meta = AssignmentMetadata.now(
                                    "system",
                                    "Load test assignment " + threadId + "_" + j
                            );

                            if (j % 2 == 0) {
                                assignmentManager.add(new PermanentAssignment(user, role, meta));
                            } else {
                                String expiresAt = java.time.LocalDateTime.now()
                                        .plusDays(1)
                                        .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                                assignmentManager.add(new TemporaryAssignment(user, role, meta, expiresAt));
                            }
                        } catch (IllegalArgumentException e) {
                            if (!e.getMessage().contains("уже назначена")) {
                                throw e;
                            }
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        int totalAssignments = assignmentManager.count();
        assertTrue(totalAssignments > 0, "Должны быть созданы назначения");

        long uniqueActiveAssignments = assignmentManager.getActiveAssignments().stream()
                .map(a -> a.user().username() + ":" + a.role().name())
                .distinct()
                .count();

        assertEquals(assignmentManager.getActiveAssignments().size(), uniqueActiveAssignments,
                "Активные назначения должны быть уникальными по паре пользователь:роль");
    }

    @Test
    void testConcurrentReadOperations() throws InterruptedException {
        RBACSystem system = new RBACSystem();
        UserManager userManager = system.getUserManager();
        RoleManager roleManager = system.getRoleManager();

        for (int i = 0; i < 20; i++) {
            userManager.add(User.validate("user_" + i, "User " + i, "user" + i + "@test.com"));
        }

        Role adminRole = new Role("Administrator", "Full access");
        adminRole.addPermission(new Permission("READ", "users", "View users"));
        adminRole.addPermission(new Permission("WRITE", "users", "Edit users"));
        roleManager.add(adminRole);

        Role viewerRole = new Role("Viewer", "Read-only");
        viewerRole.addPermission(new Permission("READ", "reports", "View reports"));
        roleManager.add(viewerRole);

        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
        CountDownLatch latch = new CountDownLatch(NUM_THREADS);
        int[] readCounts = new int[NUM_THREADS];

        for (int i = 0; i < NUM_THREADS; i++) {
            final int threadId = i;
            executor.execute(() -> {
                try {
                    for (int j = 0; j < OPERATIONS_PER_THREAD; j++) {
                        userManager.findByFilter(UserFilters.byUsernameContains("user"));

                        roleManager.findByFilter(RoleFilters.hasPermission("READ", "users"));

                        userManager.findAll(null, UserSorters.byUsername());

                        roleManager.findAll(null, RoleSorters.byName());

                        readCounts[threadId]++;
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        for (int i = 0; i < NUM_THREADS; i++) {
            assertEquals(OPERATIONS_PER_THREAD, readCounts[i],
                    "Поток " + i + " должен выполнить все операции");
        }

        assertEquals(20, userManager.count(), "Количество пользователей должно остаться неизменным");
        assertEquals(2, roleManager.count(), "Количество ролей должно остаться неизменным");
    }
}