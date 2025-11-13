import com.backend.domain.member.repository.MemberRepository
import com.backend.global.security.user.CustomUserDetails
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(
    private val memberRepository: MemberRepository
) : UserDetailsService {

    override fun loadUserByUsername(memberId: String): UserDetails {
        val member = memberRepository.findByMemberId(memberId)
            ?: throw UsernameNotFoundException("해당 ID의 회원을 찾을 수 없습니다.")
        return CustomUserDetails(member)
    }

    fun loadUserById(memberPk: Long): UserDetails {
        val member = memberRepository.findById(memberPk)
            .orElseThrow { UsernameNotFoundException("해당 회원을 찾을 수 없습니다.") }
        return CustomUserDetails(member)
    }
}
