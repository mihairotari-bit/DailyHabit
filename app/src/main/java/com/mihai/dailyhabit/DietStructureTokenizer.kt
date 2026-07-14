package com.mihai.dailyhabit

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DietStructureTokenizer @Inject constructor() {
    
    fun tokenize(rawText: String): List<String> {
        var text = rawText
        
        // Remove non-breaking spaces and invisible unicode characters
        text = text.replace("\u00A0", " ").replace("\u200B", "")

        // 1. Protect references
        val referencesToProtect = listOf(
            Regex("(?i)vedi\\s+alternative\\s+del\\s+pranzo") to "[[REF_LUNCH]]",
            Regex("(?i)alternative\\s+pranzo") to "[[REF_LUNCH_SHORT]]"
        )
        
        referencesToProtect.forEach { (pattern, placeholder) ->
            text = text.replace(pattern, placeholder)
        }

        // 2. Normalize common weird OCR artifacts and force newlines for structural headers:
        val replacements = mapOf(
            Regex("(?i)G\\s*I\\s*O\\s*R\\s*N\\s*O\\s+C\\s*O\\s*N\\s+A\\s*L\\s*L\\s*E\\s*N\\s*A\\s*M\\s*E\\s*N\\s*T\\s*O\\s*[:\\-]*") to "\nGIORNO CON ALLENAMENTO\n",
            Regex("(?i)G\\s*I\\s*O\\s*R\\s*N\\s*O\\s+S\\s*E\\s*N\\s*Z\\s*A\\s+A\\s*L\\s*L\\s*E\\s*N\\s*A\\s*M\\s*E\\s*N\\s*T\\s*O\\s*[:\\-]*") to "\nGIORNO SENZA ALLENAMENTO\n",
            Regex("(?i)C\\s*O\\s*L\\s*A\\s*Z\\s*I\\s*O\\s*N\\s*E\\s*[:\\-]*") to "\nCOLAZIONE\n",
            Regex("(?i)P\\s*R\\s*A\\s*N\\s*Z\\s*O\\s*[:\\-]*") to "\nPRANZO\n",
            Regex("(?i)M\\s*E\\s*R\\s*E\\s*N\\s*D\\s*A\\s*[:\\-]*") to "\nMERENDA\n",
            Regex("(?i)S\\s*P\\s*U\\s*N\\s*T\\s*I\\s*N\\s*O\\s*[:\\-]*") to "\nSPUNTINO\n",
            Regex("(?i)C\\s*E\\s*N\\s*A\\s*[:\\-]*") to "\nCENA\n",
            Regex("(?i)P\\s*R\\s*E\\s*W\\s*O\\s*U\\s*T\\s*[:\\-]*") to "\nPREWOUT\n",
            Regex("(?i)P\\s*R\\s*E\\s*\\-?\\s*W\\s*O\\s*R\\s*K\\s*O\\s*U\\s*T\\s*[:\\-]*") to "\nPRE WORKOUT\n",
            Regex("(?i)P\\s*O\\s*S\\s*T\\s*A\\s*L\\s*L\\s*E\\s*N\\s*A\\s*M\\s*E\\s*N\\s*T\\s*O\\s*[:\\-]*") to "\nPOSTALLENAMENTO\n",
            Regex("(?i)P\\s*O\\s*S\\s*T\\s*\\-?\\s*W\\s*O\\s*R\\s*K\\s*O\\s*U\\s*T\\s*[:\\-]*") to "\nPOST WORKOUT\n",
            Regex("(?i)O\\s*p\\s*z\\s*i\\s*o\\s*n\\s*e\\s*(\\d+)") to "\nOpzione $1\n",
            Regex("(?i)o\\s*p\\s*p\\s*u\\s*r\\s*e") to "\noppure ",
            Regex("\\+") to "\n+\n"
        )
        
        // Apply replacements to force line breaks around markers
        replacements.forEach { (pattern, replacement) ->
            text = text.replace(pattern, replacement)
        }
        
        // 3. Restore protected references
        text = text.replace("[[REF_LUNCH]]", "vedi alternative del pranzo")
                   .replace("[[REF_LUNCH_SHORT]]", "alternative pranzo")
        
        return text.split('\n')
            .map { it.replace(Regex("\\s+"), " ").trim() }
            .filter { it.isNotBlank() }
    }
}
