package com.android.labassist;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    private NavController navController;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        bottomNav = findViewById(R.id.bottom_navigation);
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        assert navHostFragment != null;
        navController = navHostFragment.getNavController();
        setupNavigation();

    }

    private void setupNavigation(){
        String role = RoleManager.getRole(MainActivity.this);
        switch (role){
            case "end_user":
                navController.setGraph(R.navigation.nav_end_user);
                bottomNav.inflateMenu(R.menu.menu_enduser);
                break;
            case "admin":
                            navController.setGraph(R.navigation.nav_admin);
                            bottomNav.inflateMenu(R.menu.menu_admin);
                            break;
            case "technician":
                            navController.setGraph(R.navigation.nav_technician);
                            bottomNav.inflateMenu(R.menu.menu_technician);
                            break;

            default:
                navController.setGraph(R.navigation.nav_end_user);
                bottomNav.inflateMenu(R.menu.menu_enduser);
                break;
        }

        NavigationUI.setupWithNavController(bottomNav, navController);
    }
}