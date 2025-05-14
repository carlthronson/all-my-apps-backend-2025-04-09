package personal.carl.thronson.security.data.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;
import personal.carl.thronson.budget.data.entity.TransactionEntity;
import personal.carl.thronson.security.data.core.Account;

@Entity(name = "account")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class AccountEntity extends Account {

  @Getter
  @Setter
  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(name = "account_roles", joinColumns = @JoinColumn(name = "account_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
  private Set<RoleEntity> roles;

  @Getter
  @Setter
  @JsonIgnore
  @OneToMany(mappedBy = "account")
  private List<ResetPasswordTokenEntity> tokens;

  @Getter
  @Setter
  @Transient
  private String authToken;

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
      name = "account_delegates",
      joinColumns = @JoinColumn(name = "account_id"),
      inverseJoinColumns = @JoinColumn(name = "delegate_id")
  )
  @Getter
  @Setter
  private List<AccountEntity> delegates = new ArrayList<>();

  @ManyToMany(mappedBy = "delegates")
  @Getter
  @Setter
  private List<AccountEntity> delegators = new ArrayList<>();

  @OneToMany(mappedBy = "publisher", fetch = FetchType.EAGER)
  @Getter
  @Setter
  private List<TransactionEntity> publishedTransactions = new ArrayList<>();
}
