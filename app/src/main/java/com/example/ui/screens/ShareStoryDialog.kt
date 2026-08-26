package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Story
import com.example.ui.theme.Primary
import com.example.ui.theme.PrimaryContainer
import com.example.util.StoryPdfExporter
import com.example.util.StoryShareHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareStoryBottomSheet(
  story: Story,
  onDismiss: () -> Unit,
  onExportPdf: () -> Unit
) {
  val context = LocalContext.current
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  val shareLink = StoryShareHelper.generateStoryLink(story)
  val summaryText = StoryShareHelper.generateSummaryText(story)

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = Color(0xFFFDF8FF),
    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    modifier = Modifier.testTag("share_story_bottom_sheet")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp)
        .padding(bottom = 32.dp)
        .verticalScroll(rememberScrollState())
    ) {
      // Header Bar
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Surface(
            shape = CircleShape,
            color = PrimaryContainer,
            modifier = Modifier.size(40.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = Icons.Default.Share,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(22.dp)
              )
            }
          }
          Spacer(modifier = Modifier.width(12.dp))
          Column {
            Text(
              text = "Share Story",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1D1B20)
              )
            )
            Text(
              text = "Share text summary, link, or full tale",
              style = MaterialTheme.typography.bodySmall.copy(
                color = Color(0xFF49454F),
                fontSize = 12.sp
              )
            )
          }
        }

        IconButton(
          onClick = onDismiss,
          modifier = Modifier.testTag("share_sheet_close_button")
        ) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Close share sheet",
            tint = Color(0xFF49454F)
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Share Link Card
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE6E1E5)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.Link,
            contentDescription = null,
            tint = Primary,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = shareLink,
            style = MaterialTheme.typography.bodySmall.copy(
              color = Color(0xFF49454F),
              fontFamily = FontFamily.Monospace,
              fontSize = 11.5.sp
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = PrimaryContainer,
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .clickable { StoryShareHelper.copyToClipboard(context, shareLink, "Story Link") }
              .testTag("copy_share_link_button")
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = "Copy link",
                tint = Color(0xFF21005D),
                modifier = Modifier.size(14.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "Copy",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFF21005D)
                )
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Summary Preview Card
      Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3EDF7)),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("share_summary_preview_card")
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "Summary & Teaser Preview",
                style = MaterialTheme.typography.labelMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = Primary
                )
              )
            }

            IconButton(
              onClick = { StoryShareHelper.copyToClipboard(context, summaryText, "Story Summary") },
              modifier = Modifier
                .size(28.dp)
                .testTag("copy_summary_text_button")
            ) {
              Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = "Copy summary text",
                tint = Primary,
                modifier = Modifier.size(16.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(6.dp))

          Box(
            modifier = Modifier
              .fillMaxWidth()
              .background(Color.White, RoundedCornerShape(12.dp))
              .padding(12.dp)
          ) {
            Text(
              text = summaryText,
              style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 12.sp,
                lineHeight = 17.sp,
                color = Color(0xFF2B292F)
              ),
              maxLines = 6,
              overflow = TextOverflow.Ellipsis
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Main Primary Action: Share Summary & Link
      Button(
        onClick = {
          StoryShareHelper.shareSummary(context, story)
          onDismiss()
        },
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Primary),
        modifier = Modifier
          .fillMaxWidth()
          .height(48.dp)
          .testTag("share_summary_intent_button")
      ) {
        Icon(
          imageVector = Icons.Default.Share,
          contentDescription = null,
          tint = Color.White,
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Share Summary & Link",
          style = MaterialTheme.typography.labelLarge.copy(
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Secondary Action: Share Full Story Text
      OutlinedButton(
        onClick = {
          StoryShareHelper.shareFullStory(context, story)
          onDismiss()
        },
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.5.dp, Primary),
        modifier = Modifier
          .fillMaxWidth()
          .height(48.dp)
          .testTag("share_full_text_intent_button")
      ) {
        Icon(
          imageVector = Icons.Default.Description,
          contentDescription = null,
          tint = Primary,
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Share Complete Story Text",
          style = MaterialTheme.typography.labelLarge.copy(
            fontWeight = FontWeight.Bold,
            color = Primary
          )
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Tertiary Action: Export PDF
      OutlinedButton(
        onClick = {
          onDismiss()
          onExportPdf()
        },
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color(0xFF79747E)),
        modifier = Modifier
          .fillMaxWidth()
          .height(48.dp)
          .testTag("share_sheet_export_pdf_button")
      ) {
        Icon(
          imageVector = Icons.Default.PictureAsPdf,
          contentDescription = null,
          tint = Color(0xFF49454F),
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Export as Illustrated PDF",
          style = MaterialTheme.typography.labelLarge.copy(
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF49454F)
          )
        )
      }
    }
  }
}
