require "net/http"
require "builder/xmlmarkup"

namespace :solr do
  task :indexcomments => :environment do
    Comment.find_in_batches(:batch_size => 500) do |comments|
      builder = Builder::XmlMarkup.new()
      builder.add {
        comments.each do |comment|
          builder.doc {
            builder.field(comment.id, :name => :"Id")
            builder.field(comment.text, :name => :"COMMENTS_OF_#{comment.commentable.id}")
          }
        end
      }
      updatesolr(builder.target!)
      commitsolr
    end
  end

  task :clearindex => :environment do
    updatesolr("<delete><query>*:*</query></delete>")
    commitsolr
  end

  def updatesolr(body)
    url = URI.parse("http://localhost:5000/solr/update")
    request = Net::HTTP::Post.new(url.path)
    request.content_type = "application/xml"
    request.body = body
    response = Net::HTTP.start(url.host, url.port) { |http| http.request(request) }
    puts response.to_s
    puts response.body
  end

  def commitsolr
    updatesolr("<commit/>")
  end
end


#   Example DOC
#<add>
#	<doc>
#	  <field name='Id'>8</field>
#	  <field name='COMMENTS_OF_40'>A lot of ranged attack animations utilize the same projectile with different colors which makes the game lose some flavor.   Doesn't only apply to Necro model, but his is an example.   The hero had a very distinct projectile, and I would like to see something like that in Dota, really make the ranged attacks different.</field>
#	</doc>
#	<doc>
#	  <field name='Id'>1328</field>
#	  <field name='COMMENTS_OF_40'>Liked the fact that you can clearly see his scythe now, opposed to W3 DotA. That's a nice little detail that follows his ultimate theme.
#
#As people already noted, a lot of ranged heroes that have been shown in DotA2 so far have similar attack projectiles, necro being one of them. Could use some more variety for those.</field>
#	</doc>
#	<doc>
#	  <field name='Id'>611</field>
#	  <field name='COMMENTS_OF_30'>I prefer DotA's attack animation (attacking with 2 arms shooting a wave of water) rather than DOTA 2's attack animation (attacking with 1 arm shooting a ball of water). Change that, and he will be perfect :D</field>
#	</doc>
#	<doc>
#	  <field name='Id'>1285</field>
#	  <field name='COMMENTS_OF_30'>MAKE HIS MODEL LOOK LIKE THE WALLPAPER PLZ.
#IT LOOKS WAY TOO SMOOTH NOW. MAKE IT ROUGH-ER AND MORE WATERY.</field>
#	</doc>
#</add>