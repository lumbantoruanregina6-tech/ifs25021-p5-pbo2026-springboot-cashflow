package org.delcom._1s25021springbootcashflow.framework.view;

import org.delcom._1s25021springbootcashflow.adapter.storage.FileStorage;
import org.delcom._1s25021springbootcashflow.domain.entity.User;
import org.delcom._1s25021springbootcashflow.domain.exception.DomainException;
import org.delcom._1s25021springbootcashflow.domain.repository.IUserRepository;
import org.delcom._1s25021springbootcashflow.framework.auth.AuthSession;
import org.delcom._1s25021springbootcashflow.usecase.UserUseCase;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.streams.UploadHandler;
import java.io.IOException;

@Route(value = "profile", layout = MainLayout.class)
@PageTitle("Profil | Cashflow")
public class ProfileView extends VerticalLayout {
    private final UserUseCase userUseCase;
    private final IUserRepository users;
    private final FileStorage storage;

    private final TextField fullName = new TextField("Nama lengkap");
    private final PasswordField password = new PasswordField("Password baru (kosongkan jika tidak diubah)");
    private final Avatar avatar = new Avatar();
    private final Span username = new Span();
    private final ProgressBar progress = new ProgressBar();
    private User current;

    public ProfileView(UserUseCase userUseCase, IUserRepository users, FileStorage storage) {
        this.userUseCase = userUseCase;
        this.users = users;
        this.storage = storage;

        avatar.setHeight("96px");
        avatar.setWidth("96px");
        fullName.setWidthFull();
        password.setWidthFull();
        progress.setIndeterminate(true);
        progress.setVisible(false);

        UI ui = UI.getCurrent();
        Upload upload = new Upload(UploadHandler.inMemory((metadata, data) -> {
            try {
                String path = this.storage.store(metadata.fileName(), data);
                this.userUseCase.updatePhoto(current.getId(), path);
                ui.access(() -> {
                    PageCard.success("Foto profil diperbarui");
                    ui.getPage().reload();
                });
            } catch (DomainException | IOException ex) {
                ui.access(() -> PageCard.error("Gagal mengunggah foto: " + ex.getMessage()));
            }
        }));
        upload.setAcceptedFileTypes("image/png", "image/jpeg", "image/gif", "image/webp");
        upload.setMaxFiles(1);
        upload.setMaxFileSize(5 * 1024 * 1024);

        Button save = new Button("Simpan");
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        save.addClickListener(e -> {
            progress.setVisible(true);
            save.setEnabled(false);
            try {
                this.userUseCase.updateProfile(current.getId(), fullName.getValue(), password.getValue());
                PageCard.success("Profil berhasil disimpan");
                UI.getCurrent().getPage().reload();
            } catch (DomainException ex) {
                PageCard.error(ex.getMessage());
            } finally {
                progress.setVisible(false);
                save.setEnabled(true);
            }
        });

        PageCard card = new PageCard("Profil Saya");
        card.setMaxWidth("520px");
        card.add(avatar, username, upload, fullName, password, progress, save);
        add(card);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        current = AuthSession.currentUser(users);
        if (current == null) return;
        fullName.setValue(current.getFullName());
        username.setText("@" + current.getUsername() + " (" + current.getRole() + ")");
        avatar.setName(current.getFullName());
        if (current.getPhotoPath() != null && !current.getPhotoPath().isBlank()) {
            avatar.setImage(FileStorage.url(current.getPhotoPath()));
        }
    }
}
