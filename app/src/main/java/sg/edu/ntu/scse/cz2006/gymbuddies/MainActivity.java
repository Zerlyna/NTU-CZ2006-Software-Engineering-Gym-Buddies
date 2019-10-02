package sg.edu.ntu.scse.cz2006.gymbuddies;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;

import sg.edu.ntu.scse.cz2006.gymbuddies.tasks.GetProfilePicFromFirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    public FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show());
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gym_list, R.id.nav_bd_search,  R.id.nav_bd_list, R.id.nav_chat_list,
                R.id.nav_forum)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        // Check if user is supposed to be here
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            logout(); // No user found
            return;
        }
        // Check email validated for email
        firebaseUser.getProviderData();
        for (UserInfo provider : firebaseUser.getProviderData()) {
            if (!provider.getProviderId().equalsIgnoreCase("password")) continue;
            if (!firebaseUser.isEmailVerified()) {
                logout(); // Email Authentication and user not verified
                return;
            }
        }

        // Set user name and email
        View header = navigationView.getHeaderView(0);
        ((TextView) header.findViewById(R.id.email)).setText(firebaseUser.getEmail());
        ((TextView) header.findViewById(R.id.name)).setText(firebaseUser.getDisplayName());
        if (firebaseUser.getPhotoUrl() != null && !firebaseUser.getPhotoUrl().toString().equalsIgnoreCase("null"))
            new GetProfilePicFromFirebaseAuth(this, bitmap -> { if (bitmap != null) {
                RoundedBitmapDrawable roundBitmap = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
                roundBitmap.setCircular(true);
                ((ImageView) header.findViewById(R.id.profile_pic)).setImageDrawable(roundBitmap);
            } }).execute(firebaseUser.getPhotoUrl()); // Download and set as profile pic
        header.setOnClickListener(v -> {
            Intent i = new Intent(this, ProfileEditActivity.class);
            i.putExtra("view", true);
            startActivity(i);
        });

        Menu navMenu = navigationView.getMenu();
        navMenu.findItem(R.id.nav_logout).setOnMenuItemClickListener(menuItem -> { logout(); return false; });
        navMenu.findItem(R.id.nav_settings).setOnMenuItemClickListener(menuItem -> { startActivity(new Intent(this, SettingsActivity.class)); return false; });
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> fab.show());
    }

    private void logout() {
        Intent logout = new Intent(this, LoginChooserActivity.class);
        logout.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        logout.putExtra("logout", true);
        startActivity(logout);
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration) || super.onSupportNavigateUp();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
