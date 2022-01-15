package fr.thomah.roger.file;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;


@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
@Entity
@Table(name = "file_entity")
public class FileEntity {

    @Id
    @Column(nullable = false)
    private UUID id;

    @Column(nullable = false)
    private String name = "";

    @Column(nullable = false)
    private String originalName = "";

    @Column(nullable = false)
    private String directory = "";

    @Column(nullable = false)
    private String format = "";

    @Column(nullable = false)
    private String url = "";

    @Column
    private String matches = "";

    public String getFullname() {
        return name + "." + format;
    }
}
