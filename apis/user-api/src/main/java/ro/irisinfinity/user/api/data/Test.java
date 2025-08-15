package ro.irisinfinity.user.api.data;

import jakarta.persistence.*;

@Entity
@Table
public class Test {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;
}
